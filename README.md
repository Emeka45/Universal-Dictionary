# Universal Dictionary

Universal Dictionary is a lightweight Android language toolbox for fast lookup, vocabulary building and learning.

## Current capabilities

- Definitions, parts of speech and examples.
- Phonetics and pronunciation audio when supplied.
- Synonyms and antonyms.
- Typing suggestions and spelling assistance.
- Persistent saved words and search history.
- Personal vocabulary learning screen.
- Android Process Text integration for selected text.
- Light/dark system theme.
- Session caching for faster repeated lookups.
- Branded Universal Dictionary launcher icon and SVG master artwork.
- GitHub Actions debug APK build pipeline.

## Data strategy

The app's baseline lookup uses the Free Dictionary API, with Datamuse used for typing suggestions. Universal Dictionary does **not** integrate proprietary dictionary databases or licensed dictionary APIs.

This keeps the project simpler: there are no Oxford, Cambridge, Collins, Merriam-Webster or WordReference credentials, contracts, subscriptions or proprietary dictionary datasets to maintain.

The project can still add compatible open-data/offline resources in the future, provided their licenses permit the intended use.

## Product roadmap

- Offline dictionary data from appropriately licensed/open sources.
- Word of the day and notifications.
- Vocabulary quizzes and streaks.
- Specialist legal, medical, scientific, business and technology terminology from compatible sources.
- Nigerian English and Nigerian Pidgin resources from compatible/open sources.
- Richer pronunciation and inflection data where permitted.
- Production-grade privacy, accessibility and release hardening.
- Automated unit and UI tests.
- Release APK/AAB configuration.

## Build

The project uses Android Gradle Plugin 8.9.1, Kotlin 2.1.21 and Compose. Build with JDK 17+:

`gradle assembleDebug`

GitHub Actions builds and publishes the debug APK as a workflow artifact.
