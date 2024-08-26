# d6ms - Database-Backed Document Management System

Welcome to **d6ms**, a Java-based Document Management System (DMS) designed to leverage the power and reliability of databases to store and manage document LOBs (Large Objects). The project name, **d6ms**, is a nod to both **DBMS** (Database Management System) and **DMS** (Document Management System), reflecting its core functionality.

## Why d6ms?

In the current landscape, there are only a few free and open-source DMS solutions available, especially those built with Java. While specifications like JCR (Java Content Repository) aim to standardize content management, the availability of versatile, robust, and easily extensible Java-based DMS solutions remains limited.

### Centralized and Reliable

A DMS is inherently centralized and must efficiently manage binary data. Databases—whether SQL or NoSQL—are built for exactly centralized storage, and handles very well the binary content. They are mature technologies known for their reliability and efficiency, particularly in handling transactions with ACID (Atomicity, Consistency, Isolation, Durability) properties. Leveraging a database as the backend for a DMS ensures that document storage is as reliable as it is scalable.

### Simplified API with Familiar Tools

Whereas specialized DMS platforms often come with their own unique APIs, **d6ms** utilizes database tables or entities that can be accessed with standard Java technologies like JDBC or JPA. This approach not only simplifies the development process but also makes it easier for developers to integrate **d6ms** with existing systems.

### Hierarchical Organization

The organization of the DMS is hierarchical, where documents and folders are nodes within the hierarchy. This structure allows for intuitive organization and retrieval of content, resembling a typical file system.

### First Use Cases Implemented

The initial use cases implemented in **d6ms** include:
- Adding a document, a folder, or a hierarchy of filesystem folders
- Adding indexes (metadata) to a document or folder
- Archiving a document or folder
- Loading document content
- Loading document or folder metadata
- Multi-criteria document or folder search service

### Benefits of a Database-Driven Approach

By building **d6ms** on top of a database, we can take advantage of existing knowledge and best practices in areas such as:
- **Authorization and Access Management**: Reuse established methods for managing user permissions and roles.
- **Clustering and High Availability**: Implement proven techniques to ensure the system remains operational and performant under heavy loads.
- **Data Backup and Recovery**: Leverage hot and cold backup strategies to protect data integrity and ensure quick recovery in case of failure.

## Getting Started

To get started with **d6ms**, simply refer to the example usages implemented in the JUnit test cases provided in the project. These examples will guide you through the basic functionalities and how to effectively use the system.
