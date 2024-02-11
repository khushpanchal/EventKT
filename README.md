[![](https://jitpack.io/v/khushpanchal/EventKT.svg)](https://jitpack.io/#khushpanchal/EventKT)
[![](https://androidweekly.net/issues/issue-609/badge)](https://androidweekly.net/issues/issue-609)

<p align="center">
  <img width="950" src="https://raw.githubusercontent.com/khushpanchal/EventKT/master/assets/banner.jpeg" >
</p>

# Table of Contents

- [About EventKT](#about-eventkt)
- [Why use EventKT?](#why-use-eventkt)
- [How EventKT works?](#how-eventkt-works)
    - [Understanding a Single Event](#understanding-single-event)
    - [High level design](#high-level-design)
- [How to use EventKT?](#how-to-use-eventkt)
    - [Installation](#installation)
    - [Initialization](#initialization)
    - [Usage](#usage)
- [Customizing EventKT](#customizing-eventkt)
- [EventKT extensions](#eventkt-extensions)
    - [Firebase](#firebase)
    - [Mixpanel](#mixpanel)
    - [Amplitude](#amplitude)
- [Contact Me](#contact-me)

<a name="about-eventkt"></a>
# About EventKT

EventKT is an **Android analytics library** created using Kotlin. With EventKT you can easily keep track of what users are doing in your app and send that info to your server. It's designed to give you really useful insights about how users engage with your app, how well your product is doing, and how it affects your business.

In the world of EventKT, an "event" is just any action or thing users do in your app. It could be clicking a button, filling out a form, or anything that shows users are interested. EventKT keeps a close eye on these actions, helping you understand patterns, see how users move through your app, and figure out important business stuff. It's like having a complete picture of how well your product is performing.

### [Get Started - Check out Full API Reference Site](https://khushpanchal.github.io/EventKT/)

<a name="why-use-eventkt"></a>
# Why use EventKT?

- **Efficient Event Tracking** - EventKT operates on the principle of optimizing event tracking by grouping events. Rather than making individual API calls for each event, the library groups a set of events and initiates a single API call at specified intervals.
  
- **Customization** - EventKT is highly customizable like grouping intervals, defining custom event thresholds, or configuring network-related settings.
  
- **Event Caching Mechanism** - Ensuring the reliability of your data, EventKT incorporates a two-tier caching mechanism. Events are cached both in memory and on disk, providing resilience in scenarios such as app crashes or forceful terminations. With EventKT, you can rest assured that your valuable event data is never lost.
  
- **Fully Kotlin and User-Friendly** - Completely built in Kotlin, EventKT leverages the expressive and concise syntax of the language. EventKT is designed with simplicity in mind. Its user-friendly interface ensures that integrating analytics into your app is a straightforward process.
  
- **Integration with Third-Party Trackers** - EventKT extends its functionality through easy integration with third-party analytics trackers. This allows you to use both in-house analytics and other popular trackers simultaneously, providing a complete view of your app's performance.
  
- **Networking Flexibility** - EventKT provides the option for clients to opt out of automatic networking. Instead, clients can choose to receive a group of events at their preferred intervals and handle the network calls independently.

With its focus on efficiency, flexibility, and seamless integration, EventKT stands as a versatile and powerful analytics solution for Android developers.

<a name="how-eventkt-works"></a>
# How EventKT works?

<a name="understanding-single-event"></a>
### Understanding a Single Event

In the realm of EventKT, a single event is composed of two crucial components:

**Event Name**: The identifier for the specific user action or behavior.

**Parameters**: Key-value pairs associated with the event, providing contextual information.

In addition to the event-specific components, there are also Base Parameters which are key-value pairs included with every event, enriching the data collected.

<a name="high-level-design"></a>
### High level design

<p align="center">
  <img width="500" alt = "High level design" src=https://raw.githubusercontent.com/khushpanchal/EventKT/master/assets/high-level-design.jpeg >
</p>

- **ITracker** - Interface containing track methods. Implemented by EventKtTracker (Main core class of EventKT library), FirebaseTracker, MixpanelTracker, AmplitudeTracker.
  
- **EventTracker** - Core class interacting with the client. Manages all trackers and delegates calls to specific trackers. Adds base parameters to each event before delegating the tracking call to individual trackers.
  
- **EventKtTracker** - The central class of the library, serving as the starting point for the entire framework. Initiates and creates all dependencies required by the library. Has access to EventManager.
  
- **EventManager** - Manages events, handles grouping logic, network state, and interactions with caching and networking classes. Manages the state of each event, providing a transactional approach to safeguard against crashes.
  
- **IGroupEventListener** - Interface containing onEventGrouped method which gets invoked every time a group of events are ready for network call.
  
  - **NetworkCallManager** - Library makes the POST API call with help of API URL and API key passed by client.
    
  - **ClientCallbackProvider** - Library invokes the lambda function and client can make the network call itself.
    
- **ICacheScheme** - Interface containing various methods related to storage of events in memory and disk.
  
  - **InMemoryCacheManager** - Responsible for keeping the list of events in the memory.
    
  - **FileCacheManager** - Responsible for keeping the list of events in the disk to safeguard from crashes. It keep itself in sync with memory.

<a name="how-to-use-eventkt"></a>
# How to use EventKT?

<a name="installation"></a>
### Installation

To integrate EventKT library into your Android project, follow these simple steps:

1. Update your settings.gradle file with the following dependency.
   
```Groovy
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' } // this one
  }
}
```

2. Update your module level build.gradle file with the following dependency.
   
```Groovy
dependencies {
  implementation 'com.github.khushpanchal.EventKT:eventkt:0.1.0'
}
```

<a name="initialization"></a>
### Initialization

Two ways of initialization:

1. Pass unique API key and API URL while initialization and library makes the API call at appropriate intervals.
   
  - API contract used by library.
    
```
{
 “events”: [
  {
   “event”: “event 1”,
   “parameters”: {
    “param1”: “value1”,
    “param2”: “value2”
   }
  },
  …
  …
 ]
}
```

  - Library will add the below header in POST API call.
    
``` 
"x-api-key" = "apiKey sent by client"
```

  - To initialize.
    
```Kotlin
class MainApplication : Application() {

  lateinit var eventTracker: EventTracker

  override fun onCreate() {
    super.onCreate()
    eventTracker = EventTracker.Builder().addTracker(
      EventKtTracker.init(
        context = this, 
        apiUrl = "your API URL",
        apiKey = "your API Key"
      )
    ).build()
  }
}
```

2. Pass the unique directory name and library will give timely callback with list of events, and client can make network call.
   
```Kotlin
class MainApplication : Application() {

  lateinit var eventTracker: EventTracker

  override fun onCreate() {
    super.onCreate()
    eventTracker = EventTracker.Builder().addTracker(
      EventKtTracker.initWithCallback(
        context = this,
        directoryName = "unique directory name", // library will use this for storing event, so it should be unique
      ) { jsonBody, eventList ->
         // this executes on background and client need to send boolean if network call succeeds
         // every time a group is ready, make network call
         return@initWithCallback true // it is compulsory to return boolean
      }
    ).build()
  }
}
```

<a name="usage"></a>
### Usage

1. To add base parameters (includes with all the event).
   
```Kotlin
eventTracker.addBaseParams(
  hashMapOf(
    Pair("BaseKey1", "BaseValue1"),
    Pair("BaseKey2", "BaseValue2")
  )
) //multiple parameter
eventTracker.addBaseParam("time", System.currentTimeMillis()) //single parameter
```

2. To track a event.
   
```Kotlin
val parameters = hashMapOf<String, Any>()
parameters["eventSpecificKey1"] = "eventSpecificValue1"
parameters["eventSpecificKey2"] = "eventSpecificValue2"
eventTracker.track("appOpen", parameters)
```

<a name="customizing-eventkt"></a>
# Customizing EventKT

1. Deciding when to flush all events to network.
   
- By default library flushes after every 10 events.
     
- There are three ways to customize the event flushing:
     
  - Count based - Set the count of events as threshold.
      
```Kotlin
EventKtTracker.init(... // or EventKtTracker.initWithCallback(...
  eventThreshold = listOf(EventThreshold.NumBased(15)) // flushing occurs after every 15 events
  ...
)
```
      
  - Time based - Set the interval as threshold.
      
```Kotlin
EventKtTracker.init(... // or EventKtTracker.initWithCallback(...
  eventThreshold = listOf(EventThreshold.TimeBased(15000L)) // flushing occurs every 15 seconds
  ...
)
```
      
   - Size based - Set the size of events as threshold.
      
```Kotlin
EventKtTracker.init(... // or EventKtTracker.initWithCallback(...
  eventThreshold = listOf(EventThreshold.SizeBased(3072)) // flushing occurs every time total event size exceeds 3kb (3072 bytes)
  ...
)
```
      
- Client can also set multiple ways in list, library will flush events whenever any threshold occurs.
     
2. Adding headers to include in api call.
   
- Client can add additional headers which will be included in api call.
     
```Kotlin
EventKtTracker.init(...
  apiHeaders = hashMapOf("headerKey1" to "headerValue1", "headerKey2" to "headerValue2")
  ...
)
```
      
3. Setting custom Disk caching logic.
   
- Client can set custom logic to cache the events in disk. By default library caches events in File system.
     
- To set custom caching logic, create a class that extends InMemoryCacheManager, and override appropriate methods from ICacheScheme and sync disk with memory.
     
```Kotlin
class MyCacheManager : InMemoryCacheManager() {...}
EventKtTracker.init(... // or EventKtTracker.initWithCallback(...
  cacheScheme = MyCacheManager()
  ...
)
```
   
4. Setting custom limits to validate event.

- Client can set custom event limits that include length of event name, length of parameter keys, length of parameter values, no. of parameters associated with each event.
  
```Kotlin
EventKtTracker.init(... // or EventKtTracker.initWithCallback(...
  eventValidationConfig = EventValidationConfig(
    maxNameLength = 100, // Default = 120
    maxKeyLength = 100, // Default = 120
    maxValueLength = 500, // Default = 512
    maxParameters = 200 // Default = 2565
  )
  ...
)
```

5. Enable or disable logs.

- Client can enable or disable logs. Currently logs are added at following positions: 1. whenever event is sent to library, 2. whenevent list of events sent to network, 3. whenever network is successful or failed.

```Kotlin
EventKtTracker.init(... // or EventKtTracker.initWithCallback(...
  enableLogs = true // Default = false (set as BuildConfig.DEBUG)
  ...
)
```

6. Setting custom logger.

- Client can set the custom logger as well.
  
```Kotlin
class MyLogger: Logger {
  override fun log(tag: String?, msg: String?, tr: Throwable?, type: LogType) {
    //log the message here
  }
}
EventKtTracker.init(... // or EventKtTracker.initWithCallback(...
  logger = MyLogger() 
  ...
)
```

<a name="eventkt-extension"></a>
# EventKT extensions

EventKT extends its functionality by providing integration with third-party analytics trackers, allowing clients to leverage the unique capabilities of Firebase, Mixpanel, and Amplitude. Clients can easily add these extensions to the library to include additional tracking methods along with it's own in house sdk.

<a name="firebase"></a>
1. Firebase

- Add following dependency at module level build.gradle file
     
```Groovy
dependencies {
  implementation 'com.github.khushpanchal.EventKT:eventkt-firebase:0.1.0'
}
```

- Add firebase to your project. [Add firebase project](https://firebase.google.com/docs/android/setup)
     
```Kotlin
  val firebaseTracker = FirebaseTracker.init(this) // initialize the firebase
  val eventTracker = EventTracker.Builder().addTracker(firebaseTracker).build() // add firebase tracker while creating EventTracker in application onCreate()
```

<a name="mixpanel"></a>
2. Mixpanel
   
- Add following dependency at module level build.gradle file
     
```Groovy
dependencies {
  implementation 'com.github.khushpanchal.EventKT:eventkt-mixpanel:0.1.0'
}
```
   
- Get a unique token from mixpanel. [Get token from mixpanel](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     
```Kotlin
  val mixpanelTracker = MixpanelTracker.init(this, "your unique token")
  val eventTracker = EventTracker.Builder().addTracker(mixpanelTracker).build() // add mixpanel tracker while creating EventTracker in application onCreate()
```

<a name="amplitude"></a>
3. Amplitude
   
- Add following dependency at module level build.gradle file
     
```Groovy
dependencies {
  implementation 'com.github.khushpanchal.EventKT:eventkt-amplitude:0.1.0'
}
```
   
- Get a unique API key from amplitude. [Get API Key from amplitude](https://www.docs.developers.amplitude.com/analytics/find-api-credentials/)
     
```Kotlin
  val amplitudeTracker = AmplitudeTracker.init(this, "your unique API key")
  val eventTracker = EventTracker.Builder().addTracker(amplitudeTracker).build() // add amplitude tracker while creating EventTracker in application onCreate()
```

For more info, check [EventKT Reference documentation](https://khushpanchal.github.io/EventKT/index.html).

<a name="contact-me"></a>
# Contact Me

- [LinkedIn](https://www.linkedin.com/in/khush-panchal-241098170/)
- [Twitter](https://twitter.com/KhushPanchal15)
- [Gmail](mailto:khush.panchal123@gmail.com)

## If this project helps you, show love ❤️ by putting a ⭐ on [this](https://github.com/khushpanchal/EventKT) project ✌️

## License

```
   Copyright (C) 2024 Khush Panchal

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

## Blog

Check out the blog: https://medium.com/@khush.panchal123/eventkt-track-it-all-1ab20888f985

## Contribute to the project

Feel free to provide feedback, report an issue, or contribute to EventKT. Head over to [GitHub repository](https://github.com/khushpanchal/EventKT), create an issue or find the pending issue. All pull requests are welcome 😄



