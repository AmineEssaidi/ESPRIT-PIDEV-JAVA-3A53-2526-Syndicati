import os
import uvicorn
import json
import traceback
import time
from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import TypedDict, Annotated, Optional, List
import operator

# Using Gemini 1.5 with Fallback to Groq
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_groq import ChatGroq
from langchain_core.messages import HumanMessage, SystemMessage, BaseMessage
from langgraph.graph import StateGraph, END

app = FastAPI(title="Syndicati Agent Pro (Diagnostic Mode)")

# --- DIAGNOSTICS ON STARTUP ---
google_key = os.getenv("GOOGLE_API_KEY", "")
groq_key = os.getenv("GROQ_API_KEY", "")
print(f"[DIAGNOSTIC] Worker started.")
print(f"[DIAGNOSTIC] Google Key Present: {bool(google_key)} (Ends with: ...{google_key[-4:] if google_key else 'N/A'})")
print(f"[DIAGNOSTIC] Groq Key Present: {bool(groq_key)} (Ends with: ...{groq_key[-4:] if groq_key else 'N/A'})")

class ChatRequest(BaseModel):
    message: str
    session_id: str = "default"
    ui_state: Optional[dict] = None
    mode: Optional[str] = "chat"

class AgentState(TypedDict):
    messages: Annotated[list[BaseMessage], operator.add]
    ui_state: Optional[dict]
    actions: List[dict]
    reply: Optional[str]
    mode: str
    success: bool

# --- TOOLS ---
class Navigate(BaseModel):
    """Navigate to a specific route."""
    route: str = Field(description="The target route")

class Click(BaseModel):
    """Click a UI element."""
    target: str = Field(description="The ID or label of the element to click")

class Fill(BaseModel):
    """Fill a text field."""
    target: str = Field(description="The ID or placeholder")
    value: str = Field(description="The text to type")

def create_graph():
    # Try different model aliases
    model_names = ["gemini-1.5-flash-latest", "gemini-flash-latest", "gemini-1.5-flash"]
    
    # Initialize Groq Fallback
    llama = None
    if groq_key:
        try:
            llama = ChatGroq(groq_api_key=groq_key, model_name="llama-3.3-70b-versatile", temperature=0.7)
            print("[INFO] Groq Fallback Engine Initialized.")
        except Exception as e:
            print(f"[ERROR] Failed to init Groq: {e}")
    
    def call_model(state: AgentState):
        messages = state['messages']
        ui_state = state.get('ui_state')
        mode = state.get('mode', 'chat')
        
        ROUTE_MAP = {
            "home": "home",
            "services": "services",
            "about": "about",
            "profile": "profile",
            "settings": "settings",
            "dashboard": "dashboard",
            "syndicat": "services/syndicat",
            "syndicats": "services/syndicat",
            "forum": "services/forum",
            "forums": "services/forum",
            "residence": "services/residence",
            "residences": "services/residence",
            "evenement": "services/evenement",
            "evenements": "services/evenement",
            "event": "services/evenement",
            "events": "services/evenement",
        }

        system_prompt = (
            "You are the Syndicati desktop assistant. MODE: " + mode + "\n"
            "You help users navigate and interact with the Syndicati property management application.\n\n"
            "VALID NAVIGATION ROUTES:\n"
            "  home, services, about, profile, settings, dashboard\n"
            "  services/syndicat  (also: syndicat, syndic, syndicats)\n"
            "  services/forum     (also: forum, forums)\n"
            "  services/residence (also: residence, residences)\n"
            "  services/evenement (also: evenement, event, events)\n\n"
            "TOOL USAGE RULES:\n"
            "  - Use Navigate when the user asks to 'go to', 'open', 'navigate to', or 'show' a page/section.\n"
            "  - Use Click ONLY for interacting with a specific button/element on the current page, never for page navigation.\n"
            "  - Use Fill to type into text fields.\n"
            "  - ALWAYS prefer Navigate over Click for any navigation intent.\n\n"
        )
        if mode == "takeover":
            elements_str = json.dumps(ui_state.get('elements', []) if ui_state else [])
            system_prompt += "CURRENT UI ELEMENTS:\n" + elements_str + "\n"
            system_prompt += (
                "\nIMPORTANT: If the user's request is to navigate to a page, use Navigate with the exact route.\n"
                "Do NOT click navigation-unrelated buttons to fulfill a navigation request.\n"
            )
            
        forced_messages = [SystemMessage(content=system_prompt)] + messages
        response = None
        
        # 1. Try Gemini
        for m_name in model_names:
            if not google_key: break
            try:
                llm = ChatGoogleGenerativeAI(model=m_name, google_api_key=google_key, temperature=0.7)
                if mode == "takeover":
                    llm = llm.bind_tools([Navigate, Click, Fill])
                response = llm.invoke(forced_messages)
                print(f"[SUCCESS] {m_name} responded.")
                break 
            except Exception as e:
                print(f"[WARNING] Gemini {m_name} failed: {str(e)}")
                continue
        
        # 2. If Gemini failed, use Groq (Llama)
        if not response and llama:
            try:
                print("[FALLBACK] Gemini failed/exhausted. Using Groq/Llama now...")
                active_llm = llama
                if mode == "takeover":
                    active_llm = llama.bind_tools([Navigate, Click, Fill])
                response = active_llm.invoke(forced_messages)
            except Exception as e:
                print(f"[ERROR] Groq fallback also failed: {str(e)}")

        if not response:
            reply = "I'm sorry, I'm currently having trouble connecting to my AI brains (Gemini/Groq). "
            if not google_key and not groq_key:
                reply += "No API keys were found in the worker environment."
            else:
                reply += "Both services seem to be hitting limits or have invalid keys. Please check the console logs."
            return {"success": False, "reply": reply, "actions": []}

        actions = []
        reply_text = response.content
        if isinstance(reply_text, list):
            texts = []
            for block in reply_text:
                if isinstance(block, dict) and block.get('type') == 'text':
                    texts.append(block.get('text', ''))
                elif isinstance(block, str):
                    texts.append(block)
            reply_text = "\n".join(texts)
        if mode == "takeover" and response.tool_calls:
            for tc in response.tool_calls:
                t_name = tc['name'].upper()
                t_args = tc['args']
                target = str(t_args.get('target', t_args.get('route', ''))).encode('ascii', 'ignore').decode('ascii').strip()
                if t_name == "NAVIGATE": actions.append({"type": "NAVIGATE", "target": target.lower()})
                elif t_name == "CLICK": actions.append({"type": "CLICK", "target": target})
                elif t_name == "FILL": actions.append({"type": "FILL", "target": target, "value": t_args['value']})
            if not reply_text: reply_text = "Executing your request..."

        return {
            "success": True,
            "reply": str(reply_text) if reply_text else "Done.",
            "actions": actions
        }
        
    workflow = StateGraph(AgentState)
    workflow.add_node("agent", call_model)
    workflow.set_entry_point("agent")
    workflow.add_edge("agent", END)
    return workflow.compile()

agent_app = create_graph()

@app.post("/chat")
async def chat(req: ChatRequest):
    try:
        user_msg = HumanMessage(content=req.message)
        initial_state = {"messages": [user_msg], "ui_state": req.ui_state, "actions": [], "reply": None, "mode": req.mode}
        result = agent_app.invoke(initial_state)
        return result
    except Exception as e:
        traceback.print_exc()
        return {"success": False, "reply": f"Worker Error: {str(e)}"}

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8002)
