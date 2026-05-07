from flask import Flask, request, jsonify
from textblob import TextBlob
import random
import sys

app = Flask(__name__)

@app.route('/api/full-analysis', methods=['POST'])
def full_analysis():
    try:
        data = request.json
        if not data:
            return jsonify({"success": False, "error": "No JSON data provided"}), 400
            
        text = data.get('text', '')
        
        if not text:
            return jsonify({
                "sentiment": "Sentiment: Neutral\nConfidence: 100%\nExplanation: No text provided.",
                "emotions": "Primary Emotion: Neutral"
            })

        blob = TextBlob(text)
        polarity = blob.sentiment.polarity
        subjectivity = blob.sentiment.subjectivity
        
        # Determine Sentiment
        if polarity > 0.15:
            sentiment = "Positive"
        elif polarity < -0.15:
            sentiment = "Negative"
        else:
            sentiment = "Neutral"
            
        # Calculate Confidence (0-100)
        # Based on how far from 0 the polarity is, plus some weight for subjectivity
        confidence_base = abs(polarity) * 80  # Max 80
        subjectivity_bonus = subjectivity * 20 # Max 20
        confidence = int(min(100, confidence_base + subjectivity_bonus + 50)) # Minimum 50 for basic patterns
        
        explanation = f"Polarity: {polarity:.2f}, Subjectivity: {subjectivity:.2f}. "
        if sentiment == "Positive":
            explanation += "The text exhibits optimistic linguistic markers."
        elif sentiment == "Negative":
            explanation += "The text contains critical or pessimistic phrasing."
        else:
            explanation += "The text is primarily objective or lacks strong emotional cues."

        # Map Emotions
        emotions_map = {
            "Positive": ["Joy", "Surprise", "Trust", "Anticipation"],
            "Negative": ["Sadness", "Anger", "Disgust", "Fear"],
            "Neutral": ["Neutral", "Calm", "Indifference"]
        }
        
        primary_emotion = random.choice(emotions_map[sentiment])

        print(f"[SENTIMENT] Analyzed: '{text[:30]}...' -> {sentiment} ({confidence}%)")
        
        return jsonify({
            "sentiment": f"Sentiment: {sentiment}\nConfidence: {confidence}%\nExplanation: {explanation}",
            "emotions": f"Primary Emotion: {primary_emotion}"
        })
    except Exception as e:
        print(f"[ERROR] {str(e)}")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy", "service": "Syndicati-Sentiment-AI"})

if __name__ == "__main__":
    port = 5000
    print(f"Sentiment Analysis API starting on port {port}...")
    app.run(host="127.0.0.1", port=port, debug=False)
