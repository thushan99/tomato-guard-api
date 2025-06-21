# Docker Commands and Usage Guide

## File Structure
Make sure your project directory has these files:
```
your_project/
├── app.py
├── Dockerfile
├── requirements.txt
├── my_model.pt        # Your trained YOLOv8 model
└── README.md          # This file
```

## Building and Running the Docker Container

### 1. Build the Docker Image
```bash
docker build -t yolo-api .
```

### 2. Run the Container
```bash
docker run -p 5000:5000 yolo-api
```

### 3. Run in Background (Detached Mode)
```bash
docker run -d -p 5000:5000 --name yolo-api-container yolo-api
```

### 4. Stop the Container
```bash
docker stop yolo-api-container
```

### 5. Remove the Container
```bash
docker rm yolo-api-container
```

## API Endpoints

### 1. Health Check
```bash
curl http://localhost:5000/health
```

### 2. Model Information
```bash
curl http://localhost:5000/model_info
```

### 3. Predict with Image Upload
```bash
curl -X POST -F "image=@path/to/your/image.jpg" http://localhost:5000/predict
```

### 4. Predict with Base64 Image
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"image": "base64_encoded_image_string"}' \
  http://localhost:5000/predict_base64
```

## Testing Examples

### Python Test Script
```python
import requests
import base64

# Test with file upload
def test_file_upload():
    url = "http://localhost:5000/predict"
    with open("test_image.jpg", "rb") as f:
        files = {"image": f}
        response = requests.post(url, files=files)
    print("File upload response:", response.json())

# Test with base64
def test_base64():
    url = "http://localhost:5000/predict_base64"
    with open("test_image.jpg", "rb") as f:
        encoded_string = base64.b64encode(f.read()).decode()
    
    data = {"image": encoded_string}
    response = requests.post(url, json=data)
    print("Base64 response:", response.json())

# Run tests
test_file_upload()
test_base64()
```

### JavaScript Test (Browser)
```javascript
// Test with file upload
async function testFileUpload(file) {
    const formData = new FormData();
    formData.append('image', file);
    
    const response = await fetch('http://localhost:5000/predict', {
        method: 'POST',
        body: formData
    });
    
    const result = await response.json();
    console.log('Prediction result:', result);
}

// Test with base64
async function testBase64(base64Image) {
    const response = await fetch('http://localhost:5000/predict_base64', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ image: base64Image })
    });
    
    const result = await response.json();
    console.log('Prediction result:', result);
}
```

## Response Format

### Successful Detection Response
```json
{
  "status": "success",
  "detections": [
    {
      "class_name": "person",
      "class_id": 0,
      "confidence": 0.85,
      "bbox": {
        "x1": 100.5,
        "y1": 200.3,
        "x2": 300.7,
        "y2": 450.9,
        "width": 200.2,
        "height": 250.6
      }
    }
  ],
  "detection_count": 1
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Error description"
}
```

## Troubleshooting

### Common Issues:

1. **Model not found**: Make sure `my_model.pt` is in the same directory as your Dockerfile
2. **Port already in use**: Use a different port mapping like `-p 5001:5000`
3. **Out of memory**: Reduce batch size or use a smaller model variant
4. **CUDA issues**: This setup uses CPU inference. For GPU support, use nvidia/cuda base image

### Logs
```bash
# View container logs
docker logs yolo-api-container

# Follow logs in real-time
docker logs -f yolo-api-container
```

### Interactive Shell
```bash
# Access container shell for debugging
docker exec -it yolo-api-container /bin/bash
```