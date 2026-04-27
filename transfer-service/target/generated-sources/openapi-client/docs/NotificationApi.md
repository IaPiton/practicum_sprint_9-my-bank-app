# NotificationApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**notification**](NotificationApi.md#notification) | **POST** /notification | Уведомление пользователей |



## notification

> notification(notification)

Уведомление пользователей

Уведомление об операциях пользователя

### Example

```java
// Import classes:
import yandex.practicum.notification.client.ApiClient;
import yandex.practicum.notification.client.ApiException;
import yandex.practicum.notification.client.Configuration;
import yandex.practicum.notification.client.models.*;
import yandex.practicum.notification.client.api.NotificationApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        NotificationApi apiInstance = new NotificationApi(defaultClient);
        Notification notification = new Notification(); // Notification | 
        try {
            apiInstance.notification(notification);
        } catch (ApiException e) {
            System.err.println("Exception when calling NotificationApi#notification");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **notification** | [**Notification**](Notification.md)|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Уведомление отправлено |  -  |
| **400** | Ошибка при отправке уведомления |  -  |

