Android Redacter 

Privacy at your fingertips
An AI-powered Android app that automatically detects and redacts sensitive information from images and PDFs, all processed on-device. Protect personal data like names, addresses, phone numbers, IDs, or credit cards before sharing screenshots, documents, or photos. No cloud uploads, no data leaving your phone. Why Android Redacter? Everyday privacy risks: Accidentally sharing PII in screenshots, scanned docs, or photos, etc. 

Traditional tools require manual editing or send data to servers.

This app uses on-device AI (OCR + smart detection) + optional voice commands to black out sensitive text instantly.

FeaturesOn-device OCR — Extracts text from images/PDFs without internet
Smart PII detection — Automatically highlights names, emails, phones, addresses, IDs
One-tap redaction — Black out detected items or draw custom boxes
Voice commands — Say "Redact all phone numbers" or "Hide addresses" for hands-free control
Supports images & PDFs — Gallery photos, camera shots, or document scans
No data leaves device — Full privacy by design
Material 3 design — Clean, modern, dark/light themes

Tech StackKotlin + Jetpack Compose — Modern Android UI
ML Kit / TFLite — On-device text recognition and custom models
MediaPipe or custom NN — PII classification (planned)
Android Speech API — Voice command integration

Quick Start (For Testing/Development)

git clone https://github.com/redacted-actual/Android-Redacter.git
cd Android-Redacter
# Open in Android Studio
# Run on device or emulator
# Grant necessary permissions (Camera, Storage)

- Redacted
