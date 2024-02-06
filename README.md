# Daily Catholic Quotes App

## Overview

The DCQ app is an Android application that allows users to create visually appealing quote images by combining quotes with beautiful background images fetched from Unsplash. Users can save, copy, and share these quote images.

## Features

- **Fetch Daily Quotes**: Retrieve the daily quote along with its author and category from Firebase.
- **Display Quote**: Show the fetched quote of the day along with its author.
- **Notification**: Receive a notification with the quote of the day.
- **Quote Generation**: Create quote images by combining user-provided quotes with background images.
- **Image Selection**: Display a collection of images fetched from Unsplash for users to choose as backgrounds.
- **Save to Device**: Save the created quote images to the user's device storage.
- **Copy to Clipboard**: Copy the quote text to the clipboard for easy sharing or pasting elsewhere.
- **Share on Social Media**: Share the generated quote images on social media platforms.

## Getting Started

### Prerequisites

- Android Studio
- Firebase Realtime Database Access

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/DCQApp.git
   ```

2. Open the project in Android Studio.

3. Connect the app to your Firebase Realtime Database by replacing the Firebase URL in `FirebaseDatabase.getInstance()` with your database URL.

4. Build and run the app on an emulator or physical device.

## Usage

1. Open the app, and the quote of the day will be fetched and displayed.
2. If the app is running, you will receive a notification with the daily quote.

## Permissions

- Internet: Required for fetching quotes from Firebase.
- Receive Boot Completed: Required for setting up alarms to fetch daily quotes.

## Libraries Used

- [Firebase Database](https://firebase.google.com/docs/database): Realtime database provided by Firebase for storing and syncing data in real time.
- [NotificationCompat](https://developer.android.com/training/notify-user/build-notification): Android library for creating notifications that are compatible with different Android versions.

## Contributing

Contributions are welcome! If you have ideas for improvements or find issues, please create a new issue or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to Firebase for providing the Firebase Realtime Database service.
- Special thanks to the Firebase community for their support and resources.