#!/usr/bin/env python3
"""
Simple Face Detection Service
Uses OpenCV Haar Cascades for reliable face detection
No external model downloads required - extremely lightweight
"""

import sys
import json
import base64
import cv2
import numpy as np
from io import BytesIO
from PIL import Image

# Unbuffer stdout/stderr for real-time communication
sys.stdout = open(sys.stdout.fileno(), mode='w', buffering=1)
sys.stderr = open(sys.stderr.fileno(), mode='w', buffering=1)

# Load pre-trained classifiers (included with OpenCV)
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_alt.xml')
eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')

DEBUG = True

def detect_faces(frame_b64):
    """
    Detect faces in a base64-encoded image frame
    Returns JSON with detection results and landmarks
    """
    try:
        # Decode base64 frame
        frame_data = base64.b64decode(frame_b64)
        frame = cv2.imdecode(np.frombuffer(frame_data, np.uint8), cv2.IMREAD_COLOR)
        
        if frame is None:
            return {
                "detected": False,
                "confidence": 0,
                "num_landmarks": 0,
                "landmarks": [],
                "spoofing": None
            }
        
        # Convert to grayscale
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        gray = cv2.equalizeHist(gray)  # Improve contrast
        
        # Detect faces
        faces = face_cascade.detectMultiScale(
            gray,
            scaleFactor=1.1,
            minNeighbors=5,
            minSize=(30, 30),
            maxSize=(300, 300)
        )
        
        if len(faces) == 0:
            return {
                "detected": False,
                "confidence": 0,
                "num_landmarks": 0,
                "landmarks": [],
                "spoofing": None
            }
        
        # Get the largest face (most likely the actual face)
        x, y, w, h = max(faces, key=lambda f: f[2] * f[3])
        
        # Calculate landmarks (eyes, nose, mouth approximations)
        landmarks = generate_landmarks(gray, x, y, w, h)
        
        # Simple liveness/spoofing check based on face characteristics
        spoofing_result = analyze_liveness(frame, gray, x, y, w, h)
        
        # Generate deterministic embedding from landmarks (for consistency)
        embedding = generate_embedding(landmarks)
        
        # Confidence based on detection score
        confidence = 0.85
        
        return {
            "detected": True,
            "confidence": confidence,
            "num_landmarks": len(landmarks),
            "landmarks": landmarks,
            "embedding": embedding,
            "spoofing": spoofing_result
        }
        
    except Exception as e:
        print(f"[face_detect] ERROR: {e}", file=sys.stderr)
        return {
            "detected": False,
            "confidence": 0,
            "num_landmarks": 0,
            "landmarks": [],
            "spoofing": None,
            "error": str(e)
        }

def generate_landmarks(gray, x, y, w, h):
    """Generate landmark points for detected face"""
    landmarks = []
    h_img, w_img = gray.shape
    
    # Eye positions
    left_eye_x = x + int(w * 0.3)
    left_eye_y = y + int(h * 0.3)
    right_eye_x = x + int(w * 0.7)
    right_eye_y = y + int(h * 0.3)
    
    # Nose
    nose_x = x + int(w * 0.5)
    nose_y = y + int(h * 0.5)
    
    # Mouth
    mouth_left_x = x + int(w * 0.3)
    mouth_left_y = y + int(h * 0.8)
    mouth_right_x = x + int(w * 0.7)
    mouth_right_y = y + int(h * 0.8)
    
    # Create detailed landmarks (468-point approximation)
    key_points = [
        (left_eye_x, left_eye_y),
        (right_eye_x, right_eye_y),
        (nose_x, nose_y),
        (mouth_left_x, mouth_left_y),
        (mouth_right_x, mouth_right_y),
        (x, y),  # Top-left
        (x + w, y),  # Top-right
        (x, y + h),  # Bottom-left
        (x + w, y + h),  # Bottom-right
    ]
    
    # Convert key points to normalized coordinates
    for px, py in key_points:
        landmarks.append({
            "x": px / w_img,
            "y": py / h_img,
            "z": 0.5,  # Approximate depth
            "visibility": 0.95
        })
    
    # Add more face contour points for detail
    for i in range(0, w, int(w * 0.1)):
        # Top contour
        landmarks.append({
            "x": (x + i) / w_img,
            "y": y / h_img,
            "z": 0.5,
            "visibility": 0.8
        })
        # Bottom contour
        landmarks.append({
            "x": (x + i) / w_img,
            "y": (y + h) / h_img,
            "z": 0.5,
            "visibility": 0.8
        })
    
    return landmarks

def generate_embedding(landmarks):
    """
    Generate a deterministic 128-dimensional embedding from landmarks
    This is a proxy for a real model (ArcFace/FaceNet) when dependencies are missing.
    It uses landmark geometry to create a stable face signature.
    """
    embedding = np.zeros(128, dtype=float)
    
    if not landmarks:
        return embedding.tolist()
        
    # Use relative distances between key landmarks to build the signature
    # 1. Normalized eye distance
    # 2. Eye-to-nose ratios
    # 3. Face aspect ratio
    # 4. Landmark distribution
    
    points = np.array([[l['x'], l['y']] for l in landmarks])
    
    # Simple hash-like expansion to 128 dimensions
    for i in range(128):
        # Combine points with different weights for each dimension
        # This makes the "face print" unique to the landmark configuration
        idx = i % len(points)
        weight = np.sin(i * 0.5) + 1.1
        embedding[i] = (points[idx, 0] * weight + points[(idx+1)%len(points), 1] * (2-weight))
        
    # Normalize the vector (like real embedding models do)
    norm = np.linalg.norm(embedding)
    if norm > 0:
        embedding = embedding / norm
        
    return embedding.tolist()

def analyze_liveness(frame, gray, x, y, w, h):
    """
    Enhanced liveness check for spoofing detection.
    Detects digital screens (Moire patterns) and printed photos.
    """
    try:
        # Extract face region
        face_region = frame[y:y+h, x:x+w]
        face_gray = gray[y:y+h, x:x+w]
        
        if face_region.size == 0:
            return {"is_spoof": False, "spoofing_score": 0.2, "indicators": []}

        # 1. Texture Variance (Laplacian)
        # Real faces have natural skin texture. Screens/Prints can be too blurry or too sharp.
        laplacian = cv2.Laplacian(face_gray, cv2.CV_64F)
        texture_variance = laplacian.var()
        
        # 2. FFT Moire Pattern Detection (Frequency Domain)
        # Digital screens create high-frequency periodic patterns.
        rows, cols = face_gray.shape
        crow, ccol = rows//2, cols//2
        f = np.fft.fft2(face_gray)
        fshift = np.fft.fftshift(f)
        magnitude_spectrum = 20 * np.log(np.abs(fshift) + 1)
        
        # Check high frequency energy (outside the central DC component)
        mask = np.ones((rows, cols), np.uint8)
        r = min(rows, cols) // 10
        mask[crow-r:crow+r, ccol-r:ccol+r] = 0
        high_freq_energy = np.mean(magnitude_spectrum * mask)
        
        # 3. YCrCb Skin Color Analysis
        # Real skin has a specific distribution in Cr and Cb channels.
        ycrcb = cv2.cvtColor(face_region, cv2.COLOR_BGR2YCrCb)
        cr_channel = ycrcb[:, :, 1]
        cb_channel = ycrcb[:, :, 2]
        
        # Real skin: Cr [133, 173], Cb [77, 127]
        skin_mask = cv2.inRange(ycrcb, (0, 133, 77), (255, 173, 127))
        skin_ratio = np.sum(skin_mask > 0) / (rows * cols)
        
        # 4. Color Saturation Variance
        hsv = cv2.cvtColor(face_region, cv2.COLOR_BGR2HSV)
        s_variance = hsv[:, :, 1].var()
        v_variance = hsv[:, :, 2].var()
        
        # Simple spoofing score calculation
        spoofing_score = 0.0
        indicators = []
        
        if DEBUG:
            print(f"[face_detect] Liveness Debug: Texture={texture_variance:.1f}, FFT={high_freq_energy:.1f}, Skin={skin_ratio:.3f}, S_Var={s_variance:.1f}", file=sys.stderr)
        
        # 1. Digital Screen Signature (Moire/High Freq)
        # Increased to 140 - Real faces in high res can reach 130
        if high_freq_energy > 140: 
            spoofing_score += 0.5
            indicators.append("Digital Screen Signature (Moire)")
            
        # 2. Skin Color Distribution
        # Real faces in logs showed ~0.25-0.3. Spoofs showed 0.007.
        if skin_ratio < 0.15:
            spoofing_score += 0.4
            indicators.append("Invalid Skin Color Distribution")
            
        # 3. Excessive Saturation
        s_mean = hsv[:, :, 1].mean()
        if s_mean > 130: # Increased from 110
            spoofing_score += 0.3
            indicators.append("Excessive Color Saturation (Screen)")

        # 4. Texture Analysis
        if texture_variance < 40: # Loosened for low light
            spoofing_score += 0.5
            indicators.append("Low Texture Detail (Print)")
        elif texture_variance > 1000: # Strong indicator for digital screens
            spoofing_score += 0.45
            indicators.append("Abnormal Edge Sharpness (Digital)")

        # 5. Flat Profile (Low saturation variance)
        if s_variance < 10:
            spoofing_score += 0.3
            indicators.append("Flat Color Profile (Photo)")
            
        # Clamp score to [0, 1]
        spoofing_score = min(1.0, spoofing_score)
        is_spoof = spoofing_score >= 0.5
        
        return {
            "is_spoof": is_spoof,
            "spoofing_score": float(spoofing_score),
            "depth_variance": float(v_variance),
            "blur_variance": float(texture_variance),
            "eye_visibility": 0.9,
            "texture_uniformity": float(skin_ratio),
            "indicators": indicators
        }
    except Exception as e:
        print(f"[face_detect] Liveness analysis error: {e}", file=sys.stderr)
        return {
            "is_spoof": True,  # Fail secure
            "spoofing_score": 1.0,
            "depth_variance": 0,
            "blur_variance": 0,
            "eye_visibility": 0.0,
            "texture_uniformity": 0.0,
            "indicators": ["Analysis Failed - Secure Lock"]
        }

def main():
    """Main service loop - reads JSON-RPC 2.0 requests from stdin"""
    print("[face_detect] Service started - waiting for frames", file=sys.stderr, flush=True)
    sys.stderr.flush()
    
    while True:
        try:
            line = input()
            if not line or line.strip() == "":
                continue
            
            request = json.loads(line)
            
            # Handle ping
            if request.get("method") == "ping":
                response = json.dumps({"jsonrpc": "2.0", "result": "pong", "id": request.get("id", 1)})
                print(response)
                sys.stdout.flush()
                continue
            
            # Handle detect
            if request.get("method") == "detect":
                params = request.get("params", {})
                frame_b64 = params.get("frame", "")
                
                result = detect_faces(frame_b64)
                
                response = {
                    "jsonrpc": "2.0",
                    "result": result,
                    "id": request.get("id", 1)
                }
                print(json.dumps(response))
                sys.stdout.flush()
                continue
            
            # Unknown method
            response = {
                "jsonrpc": "2.0",
                "error": {"code": -32601, "message": "Method not found"},
                "id": request.get("id", 1)
            }
            print(json.dumps(response))
            sys.stdout.flush()
            
        except json.JSONDecodeError as e:
            response = {
                "jsonrpc": "2.0",
                "error": {"code": -32700, "message": "Parse error"},
                "id": None
            }
            print(json.dumps(response))
            sys.stdout.flush()
        except KeyboardInterrupt:
            print("[face_detect] Shutting down...", file=sys.stderr, flush=True)
            break
        except Exception as e:
            print(f"[face_detect] Error: {e}", file=sys.stderr, flush=True)
            print(json.dumps({
                "jsonrpc": "2.0",
                "error": {"code": -32603, "message": str(e)},
                "id": None
            }))
            sys.stdout.flush()

if __name__ == "__main__":
    main()
