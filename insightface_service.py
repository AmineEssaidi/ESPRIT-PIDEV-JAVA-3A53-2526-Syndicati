#!/usr/bin/env python3
"""
Face Detection Service using InsightFace
Provides face detection, landmarks, and embeddings via JSON-RPC over stdio
Auto-downloads models on first use

Python: 3.8+
Requires: insightface, onnxruntime
"""

import cv2
import numpy as np
import json
import sys
import base64
from typing import Dict, List, Optional
import logging

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stderr
)
logger = logging.getLogger('InsightFace')

# Import InsightFace
try:
    import insightface
    logger.info("✓ InsightFace library loaded")
except ImportError:
    logger.error("ERROR: insightface not installed")
    logger.error("Install with: pip install insightface onnxruntime")
    sys.exit(1)


class FaceDetector:
    """InsightFace-based face detection and recognition"""
    
    def __init__(self):
        """Initialize InsightFace detector"""
        logger.info("Initializing InsightFace...")
        
        try:
            # Initialize with default model (buffalo_sc = small + fast)
            self.app = insightface.app.FaceAnalysis(
                name='buffalo_sc',
                providers=['CPUProvider']
            )
            
            # Prepare: downloads models if needed
            self.app.prepare(ctx_id=-1, det_model='retinaface', rec_model='arcface')
            
            logger.info("✓ InsightFace initialized successfully")
            logger.info("  Detection model: RetinaFace (fast & accurate)")
            logger.info("  Recognition: ArcFace (embeddings)")
            
        except Exception as e:
            logger.error(f"ERROR: {e}")
            try:
                logger.info("Trying compatibility mode...")
                self.app = insightface.app.FaceAnalysis()
                self.app.prepare(ctx_id=-1)
                logger.info("✓ InsightFace ready (compatibility mode)")
            except Exception as e2:
                logger.error(f"FATAL: {e2}")
                sys.exit(1)
    
    def base64_to_image(self, b64: str) -> Optional[np.ndarray]:
        """Decode base64 to image"""
        try:
            data = base64.b64decode(b64)
            arr = np.frombuffer(data, np.uint8)
            return cv2.imdecode(arr, cv2.IMREAD_COLOR)
        except Exception as e:
            logger.error(f"Decode error: {e}")
            return None
    
    def image_to_base64(self, img: np.ndarray) -> str:
        """Encode image to base64"""
        try:
            _, buf = cv2.imencode('.jpg', img, [cv2.IMWRITE_JPEG_QUALITY, 95])
            return base64.b64encode(buf).tobytes().decode()
        except Exception as e:
            logger.error(f"Encode error: {e}")
            return ""
    
    def detect_faces(self, frame: np.ndarray) -> Dict:
        """Detect all faces in frame"""
        result = {'detected': False, 'faces': [], 'count': 0}
        
        try:
            faces = self.app.get(frame)
            
            if len(faces) == 0:
                return result
            
            result['detected'] = True
            result['count'] = len(faces)
            
            for i, face in enumerate(faces):
                bbox = face.bbox
                x1, y1, x2, y2 = [int(v) for v in bbox]
                
                face_data = {
                    'id': i,
                    'bbox': [x1, y1, x2, y2],
                    'x': x1,
                    'y': y1,
                    'width': x2 - x1,
                    'height': y2 - y1,
                    'confidence': float(face.det_score) if hasattr(face, 'det_score') else 0.95,
                }
                
                # Landmarks
                if hasattr(face, 'kps') and face.kps is not None:
                    face_data['landmarks'] = [
                        {'x': float(p[0]), 'y': float(p[1])}
                        for p in face.kps
                    ]
                
                # Demographics
                if hasattr(face, 'age'):
                    face_data['age'] = int(face.age)
                if hasattr(face, 'gender'):
                    face_data['gender'] = int(face.gender)
                
                # Embedding for recognition
                if hasattr(face, 'embedding'):
                    face_data['embedding'] = face.embedding.tolist()
                
                result['faces'].append(face_data)
            
            return result
        
        except Exception as e:
            logger.error(f"Detection error: {e}")
            return result
    
    def check_liveness(self, frames: List[np.ndarray]) -> Dict:
        """Check if face is real (liveness detection)"""
        if len(frames) < 2:
            return {'is_real': True, 'confidence': 0.5}
        
        try:
            detections = []
            for frame in frames:
                faces = self.app.get(frame)
                if len(faces) > 0:
                    detections.append(faces[0])
            
            if len(detections) < 2:
                return {'is_real': False, 'confidence': 0.2}
            
            # Check embedding consistency
            embeddings = [f.embedding for f in detections if hasattr(f, 'embedding')]
            
            if len(embeddings) >= 2:
                try:
                    from scipy.spatial.distance import cosine
                    
                    distances = []
                    for i in range(len(embeddings) - 1):
                        dist = cosine(embeddings[i], embeddings[i+1])
                        distances.append(dist)
                    
                    avg_dist = np.mean(distances)
                    is_real = 0.1 < avg_dist < 0.5
                    conf = max(0, min(1.0, 1.0 - (avg_dist / 1.0)))
                    
                    return {
                        'is_real': is_real,
                        'confidence': float(conf),
                        'distance': float(avg_dist)
                    }
                except ImportError:
                    pass
            
            # Fallback: check movement
            boxes = [f.bbox for f in detections]
            movement = np.std([box[0] for box in boxes])
            
            return {
                'is_real': movement > 3.0,
                'confidence': min(movement / 15.0, 1.0),
                'movement': float(movement)
            }
        
        except Exception as e:
            logger.error(f"Liveness error: {e}")
            return {'is_real': True, 'confidence': 0.0}


def rpc_handler(detector: FaceDetector, req: Dict) -> Dict:
    """Handle JSON-RPC 2.0 requests"""
    
    method = req.get('method', '')
    params = req.get('params', {})
    mid = req.get('id', None)
    
    try:
        if method == 'detect':
            frame = detector.base64_to_image(params.get('frame', ''))
            if frame is None:
                return {
                    'jsonrpc': '2.0',
                    'error': {'code': -32603, 'message': 'Invalid frame'},
                    'id': mid
                }
            
            result = detector.detect_faces(frame)
            return {'jsonrpc': '2.0', 'result': result, 'id': mid}
        
        elif method == 'liveness':
            frames = [
                detector.base64_to_image(f) 
                for f in params.get('frames', [])
            ]
            frames = [f for f in frames if f is not None]
            
            result = detector.check_liveness(frames)
            return {'jsonrpc': '2.0', 'result': result, 'id': mid}
        
        elif method == 'ping':
            return {
                'jsonrpc': '2.0',
                'result': {'status': 'ready'},
                'id': mid
            }
        
        else:
            return {
                'jsonrpc': '2.0',
                'error': {'code': -32601, 'message': f'Unknown method: {method}'},
                'id': mid
            }
    
    except Exception as e:
        logger.error(f"Error: {e}")
        return {
            'jsonrpc': '2.0',
            'error': {'code': -32603, 'message': str(e)},
            'id': mid
        }


def main():
    """Main service loop"""
    logger.info("Starting InsightFace Service")
    
    try:
        detector = FaceDetector()
    except Exception as e:
        logger.error(f"FATAL: {e}")
        sys.exit(1)
    
    logger.info("✓ Ready for commands")
    
    try:
        for line in sys.stdin:
            line = line.strip()
            if not line:
                continue
            
            try:
                req = json.loads(line)
                resp = rpc_handler(detector, req)
                print(json.dumps(resp, separators=(',', ':')), flush=True)
            
            except json.JSONDecodeError as e:
                logger.error(f"JSON error: {e}")
                print(json.dumps({
                    'jsonrpc': '2.0',
                    'error': {'code': -32700, 'message': 'Parse error'},
                    'id': None
                }), flush=True)
    
    except KeyboardInterrupt:
        logger.info("Stopped")
    except Exception as e:
        logger.error(f"Fatal: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()
