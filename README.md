# Android Game – Spaceship Runner

An interactive Android game where the player controls a spaceship, avoids asteroids, and collects coins. The game includes two control modes (button or sensor), a dynamic score system, and a high score table with location tracking.

## Features

- Two control modes: Buttons and Accelerometer
- Heart-based life system (3 hits = game over)
- Coin collection for extra points
- Top 10 high scores saved locally
- Location stored for each high score
- Interactive high scores screen:
  - A list of the top 10 distances
  - A map displaying where each record was achieved
  - Clicking a score highlights its location on the map
- Main menu with buttons to start the game or view the high scores

## Project Structure

- `MenuActivity`: Main menu screen
- `MainActivity`: The game engine and gameplay logic
- `ScoreActivity`: Displays the current game result and saves the score
- `HighScoresActivity`: Hosts the score table and map
- `HighScoresFragment`: Shows the top 10 scores
- `MapFragment`: Displays a map with the locations of the scores

## Technologies Used

- Kotlin & Android Jetpack
- Google Maps API
- SharedPreferences for data persistence
- Material Components UI
- Optional: Firebase Analytics

## Layout Files

- `activity_menu.xml`: Main menu layout
- `activity_main.xml`: Game screen layout
- `activity_score.xml`: Score screen layout
- `activity_high_scores.xml`: Container for fragments
- `fragment_high_scores.xml`: Score list layout
- `fragment_map.xml`: Map layout

## High Score Format (Stored in SharedPreferences)

Scores are stored as a `StringSet` in JSON-like format
