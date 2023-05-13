# AAD Assignments 3-5

This app displays recent news from a RSS Newsfeed. It uses proper MVVM state
management and all news data is being cached in a local `SQLite` database. It allows the user to set some settings and uses the local device theme as app theme in
accordance to Googles' new [Material You](https://material.io/blog/announcing-material-you)
design philosophy. 

<img src="docs/app.png" width=40% height=40%>

It uses:

* Jetpack Compose
* MVVM Architecture
* ViewModels to persist data across configuration changes
* `coil` for asynchronous image fetching
* `datastore` for persisting user preferences
* `Room` for local `SQLite` caching of news data

For details have a look at the separate [assignments](assignments).
