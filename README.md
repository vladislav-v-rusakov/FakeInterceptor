# FakeInterceptor for OkHttpClient
Two custom Interceptors for OkHttpClient. 
Used to mock API responses with local json files from project assets folder.

## 1. FakeInterceptorV2.kt 
Interceptor for simple cases, just returns one specified json file from project assets.
### How to use
* create assets folder in your project
* add json file to assets folder foo.json
* add interceptor to OkHttpClient and pass context and file name to interceptor constructor
  ```
    val okHttpBuilder = OkHttpClient.Builder()
    okHttpBuilder.addInterceptor(FakeInterceptorV2(context, "foo.json")) 
  ```


## 2. FakeInterceptor.kt
Interceptor for complex cases with own build flavor.
### How to use
* create assets folder in your project
* add folder for base url  eg **mock.api**
* add sub-folders and json files according with api requests.

   ![image](https://github.com/Vladus177/FakeInterceptor/blob/master/mock.api%20assets.png?raw=true)

   **path** *assets/mock.api/users/users_get.json* for url *http://mock.api/users*

* add new Flavor for **dummy** build in module gradle

  ```
    flavorDimensions "tier"
    productFlavors {
        prod {
            dimension "tier"
            buildConfigField "String", "URL_BASE", "\"https://jsonplaceholder.typicode.com/\""
        }
        dummy {
            dimension "tier"
            buildConfigField "String", "URL_BASE", "\"http://mock.api/\""
        }
    }
  ```
* add base url to Retrofit initialization
  ```
    Retrofit.Builder()
      .client(okHttpBuilder.build())
      .baseUrl(BuildConfig.URL_BASE)
      .build()
      .create(FooRestApi::class.java)
    
  ```
* add interceptor to OkHttpClient  
  ```
    if (BuildConfig.FLAVOR.equals("dummy")) {
      okHttpBuilder.addInterceptor(FakeInterceptor(context))
    }
  ```
  
  Example project https://github.com/Vladus177/Albums
  
