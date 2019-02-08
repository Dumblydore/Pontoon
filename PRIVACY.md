# Privacy Policy

If you do not log in to Floatplane, then it will not store any information about you.

If you choose to log in using your Floatplane account, [LinusTechTips Forums](https://linustechtips.com/), or [Discord](https://discordapp.com/) then the following information will be stored **locally** on your device:
- **cfuid** & **sid** identifying your Floatplane account. You can revoke this by logging out.
- Your **user id**, **profile image url**, & **username** are stored and presented in the UI later. This information can be deleted by logging out.

This behavior is implemented [here](https://github.com/Dumblydore/Pontoon/blob/master/app/src/main/java/me/mauricee/pontoon/domain/account/AccountManagerHelper.kt).
## Analytics
This app uses Google Anayltics, Crashlytics and Performance Monitoring.

### Google Anaylitics
The app sends unidentifiable **user actions**, **app state**, and **device model**

This behavior is implemented [here](https://github.com/Dumblydore/Pontoon/blob/master/app/src/main/java/me/mauricee/pontoon/analytics/FirebaseTracker.kt).

### Crashlytics
The app sends stack traces and generic device.

Behavior for Crashlytics is implemented [here](https://github.com/Dumblydore/Pontoon/blob/master/app/src/main/java/me/mauricee/pontoon/BaseActivity.kt).

### Performance Monitoring
The app sends unidentifiable **request size**, **request url**, request method**, **response code** & **response size**

Behavior for Performance Monitoring [here](https://github.com/Dumblydore/Pontoon/blob/master/app/src/main/java/me/mauricee/pontoon/analytics/FirebaseNetworkInterceptor.kt).
