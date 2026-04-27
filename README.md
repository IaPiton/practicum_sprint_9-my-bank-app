# MY-BANK-APP
Для запуска приложения:
1. Запустить Docker, т.к он потребуется для тестов приложения.
2. Добавте в файл C:\Windows\System32\drivers\etc\hosts 
   keycloak-bank   127.0.0.1
3. Выполните команду для сборки приложения mvn clean install
4. Выполнить команду docker compose up
5. Перейдите по адресу http://localhost:8081
6. зарегистрируйте несколько пользователей.