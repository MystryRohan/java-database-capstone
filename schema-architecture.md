# Section - 1
This Spring Boot application uses both MVC and REST controllers. It uses Thymeleaf templates for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases — MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. <br>
The application is divided into 3 Layer Architecture - (Presentation, Application and Data) <br>
Presentation -> Views / Frontend <br>
Application -> APIs <br>
Data -> Databases <br>

# Section - 2
1. User accesses AdminDashboard or Appointment pages.
2. The action is routed to the appropriate Thymeleaf or REST controller.
3. The controller calls the service layer.
4. The service layers calls the repository layer.
5. The repository layers provides abstraction to communicate with the database and handles most of the configs.
6. Model binding helps converting the POJO to a database entity/ document.
7. Application Model or Model is the structure of data that is sent to the frontend via APIs.
