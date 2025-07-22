<h1 align="left">Library System 📚</h1>
<p align="left">A monorepo containing multiple services for a library system.</p>

<p align="left">
  <a href="https://github.com/l4yoos/library/commits/main">
    <img src="https://img.shields.io/github/last-commit/l4yoos/library" alt="Last Commit">
  </a>
  <a href="https://github.com/l4yoos/library">
    <img src="https://img.shields.io/github/languages/top/l4yoos/library" alt="Top Language">
  </a>
  <a href="https://github.com/l4yoos/library">
    <img src="https://img.shields.io/github/languages/count/l4yoos/library" alt="Language Count">
  </a>
</p>

<hr/>

<h2 align="left" id="overview">🚀 Overview</h2>
<p align="left">This project consists of multiple services, including book-service, notification-service, user-service, and loan-service, built using Spring Boot and Maven.</p>
<ul align="left">
  <li>book-service: manages books in the library system</li>
  <li>notification-service: handles notifications for the library system</li>
  <li>user-service: manages user interactions with the library system</li>
  <li>loan-service: handles book loans and returns</li>
</ul>

<hr/>

<h2 align="left" id="built-with">📦 Built With</h2>
<p align="left">This project uses the following technologies:</p>
<div align="left">
  <img src="https://img.shields.io/badge/Java 17-007396?logo=java&logoColor=white&style=for-the-badge" height="30" alt="Java logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?logo=spring-boot&logoColor=white&style=for-the-badge" height="30" alt="Spring Boot logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?logo=spring&logoColor=white&style=for-the-badge" height="30" alt="Spring Data JPA logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Spring_WebFlux-6DB33F?logo=spring&logoColor=white&style=for-the-badge" height="30" alt="Spring WebFlux logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Lombok-A01083?logo=lombok&logoColor=white&style=for-the-badge" height="30" alt="Lombok logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Apache_Kafka-231F20?logo=apachekafka&logoColor=white&style=for-the-badge" height="30" alt="Apache Kafka logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white&style=for-the-badge" height="30" alt="PostgreSQL logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/JUnit5-25A642?logo=junit5&logoColor=white&style=for-the-badge" height="30" alt="JUnit logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Mockito-3776AB?logo=mockito&logoColor=white&style=for-the-badge" height="30" alt="Mockito logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=white&style=for-the-badge" height="30" alt="GitHub Actions logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white&style=for-the-badge" height="30" alt="Docker logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black&style=for-the-badge" height="30" alt="Swagger logo" />
  <img width="12" />
  <img src="https://img.shields.io/badge/Kubernetes-326CE5?logo=kubernetes&logoColor=white&style=for-the-badge" height="30" alt="Kubernetes logo" />
</div>

<hr/>

<h2 align="left" id="table-of-contents">📚 Table of Contents</h2>
<p align="left">This README is organized into the following sections:</p>
<ul align="left">
  <li><a href="#overview">Overview</a></li>
  <li><a href="#built-with">Built With</a></li>
  <li><a href="#table-of-contents">Table of Contents</a></li>
  <li><a href="#architecture">Architecture</a></li>
  <li><a href="#prerequisites">Prerequisites</a></li>
  <li><a href="#installation">Installation</a></li>
  <li><a href="#usage">Usage</a></li>
  <li><a href="#testing">Testing</a></li>
  <li><a href="#demo">Demo</a></li>
</ul>

<hr/>

<h2 align="left" id="architecture">🏗️ Architecture</h2>
<p align="left">This project follows a microservices architecture, with each service (book-service, notification-service, user-service, and loan-service) being a separate module.</p>
<p align="left">Each service has its own Spring Boot application, with dependencies on other services and libraries as needed.</p>

<hr/>

<h2 align="left" id="prerequisites">✅ Prerequisites</h2>
<p align="left">To run this project, you need:</p>
<ul align="left">
  <li>Java 17</li>
  <li>Maven</li>
  <li>PostgreSQL</li>
  <li>Kafka</li>
</ul>

<hr/>

<h2 align="left" id="installation">🛠️ Installation</h2>
<p align="left">To install and run each service, follow the instructions in the respective service's README.</p>

<hr/>

<h2 align="left" id="usage">🚀 Usage</h2>
<p align="left">To use the project, you need to start each service and then use the RESTful APIs to interact with the services.</p>
<ul align="left">
  <li>Book Service: `http://localhost:8880/books`</li>
  <li>User Service: `http://localhost:8881/users`</li>
  <li>Loan Service: `http://localhost:8882/loans`</li>
  <li>Auth Service: `http://localhost:8884/auth`</li>
</ul>

<hr/>

<h2 align="left" id="testing">🚀 Postman</h2>
<p align="left">
    <a href="https://www.postman.com/l4yoosek/workspace/library/collection/29730936-e8bd00f8-03b1-4a71-9fd8-8daeb8fb8680?action=share&creator=29730936"> Collection with API</a>
</p>


<h2 align="left" id="testing">🧪 Testing</h2>
<p align="left">The project includes test files for each service.</p>

<hr/>

<h2 align="left" id="demo">🎬 Demo</h2>
<p align="left">A demo of the project is not available.</p>

> 📝 **Note**: Replace {user} and {project} in the badge URLs with your actual GitHub username and repository name.