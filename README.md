# 🍅 Tomato Guard API

Tomato Guard API is a RESTful backend system built with Spring Boot that supports intelligent tomato crop management. It integrates image recognition models and herbicide suggestions to help farmers and agricultural experts manage weed growth effectively.

---

## 🚀 Features

- 🔍 Weed detection using image classification (VGG16 and YOLOv8)
- 🧠 ML-based herbicide suggestion using trained models
- 📦 REST API for uploading images and retrieving predictions
- 🌤️ Weather data integration (external API)
- 🗂️ Analysis history tracking via database
- 🐳 Dockerized services for scalable deployment

---

## 🛠️ Tech Stack

| Layer            | Technology                 |
|------------------|----------------------------|
| Backend          | Spring Boot (Java)         |
| ML Models        | Python, Keras, PyTorch     |
| API Clients      | RestTemplate, JSON         |
| Build Tool       | Maven                      |
| Containerization | Docker, Docker Compose     |
| Persistence      | JPA, MySQL                 |

---

## 📁 Project Structure

tomato-guard-api/
├── tomato-api/ # Spring Boot REST API
│ ├── src/
│ ├── pom.xml
│ └── Dockerfile
├── mlModels/ # Python ML model files
│ ├── app.py
│ ├── application_rate_model.pkl
│ ├── herbicide_model.pkl
│ ├── requirements.txt
├── vgg16/ # VGG16 image model
│ ├── model_server.py
│ ├── tomato_weed_identify_model.h5
│ ├── requirements.txt
│ └── Dockerfile
├── yolov8/ # YOLOv8 image model
│ ├── app.py
│ ├── my_model.pt
│ ├── requirements.txt
│ └── Dockerfile
└── README.md


---

## ⚙️ Setup Instructions

### Clone the repository
```bash
git clone https://github.com/thushan99/tomato-guard-api.git
cd tomato-guard-api
---
### Models link
https://drive.google.com/drive/folders/10WAnFbHwc8rARbDEgI2DqJMuA68Ff9tN?usp=sharing
