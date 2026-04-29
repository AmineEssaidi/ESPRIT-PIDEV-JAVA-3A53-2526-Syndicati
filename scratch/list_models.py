import os
import google.generativeai as genai

api_key = "AIzaSyD_be9IsVazrlfA_Fu3Y-VS_YGB3-9gg9Q"
genai.configure(api_key=api_key)

print("Listing models...")
try:
    for m in genai.list_models():
        if 'generateContent' in m.supported_generation_methods:
            print(f"MODEL: {m.name}")
except Exception as e:
    print(f"Error: {e}")
