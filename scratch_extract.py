import cv2
import os

video_path = 'src/main/resources/login.mp4'
output_dir = 'src/main/resources/images'

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

cap = cv2.VideoCapture(video_path)
total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

# We want 4 frames evenly spaced
frame_indices = [
    int(total_frames * 0.1),
    int(total_frames * 0.35),
    int(total_frames * 0.6),
    int(total_frames * 0.85)
]

for i, f_idx in enumerate(frame_indices):
    cap.set(cv2.CAP_PROP_POS_FRAMES, f_idx)
    ret, frame = cap.read()
    if ret:
        out_path = os.path.join(output_dir, f'login_bg_{i+1}.jpg')
        cv2.imwrite(out_path, frame, [int(cv2.IMWRITE_JPEG_QUALITY), 90])
        print(f'Saved {out_path}')
    else:
        print(f'Failed to read frame {f_idx}')

cap.release()
