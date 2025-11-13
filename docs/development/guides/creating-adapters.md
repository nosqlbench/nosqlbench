+++
title = "Creating Adapters"
description = "Complete guide to implementing new NoSQLBench adapters"
weight = 20
template = "docs-page.html"

[extra]
quadrant = "development"
topic = "guides"
category = "adapters"
tags = ["development", "adapters", "drivers", "architecture"]
+++

NoSQLBench is composed of a sound architectural underpinning that makes the process of implementing new adapters as seamless and quick as possible. NoSQLBench provides a hierarchy of classes which can be extended and interfaces to be implemented, in order to avoid having to re-implement the application infrastructure and in so doing reduces both the complexity and demands on time involved in implementing a new adapter type. However, it is necessary to understand this existing architecture in order to effectively use the provided building blocks. The following is intended as a guide to developing new adapters to extend the functionality of NoSQLBench.

# Prerequisites and Assumptions
* It is assumed that the developer(s) have familiarized themselves with the [core concepts](../../explanations/concepts/core-concepts.md) provided in the NoSQLBench documentation.
* The ability to code in Java is required. NoSQLBench makes extensive use of lambda functionality. It is a requirement to understand and be comfortable using these principles to develop an adapter.
* This guide is not specific to any particular client. It is assumed that the developer(s) have familiarity with the client they are developing an adapter for.

# Internal Dependencies
The NoSQLBench project as a whole is composed of numerous modules and packages. For the purpose of customer adapter implementation there are only two required; adapters-api and nb-annotations.

	<dependency>
		<groupId>io.nosqlbench</groupId>
     	<artifactId>adapters-api</artifactId>
     	<version>${revision}</version>
     	<scope>compile</scope>
	</dependency>

	<dependency>
        <groupId>io.nosqlbench</groupId>
        <artifactId>nb-annotations</artifactId>
        <version>${revision}</version>
        <scope>compile</scope>
	</dependency>

The adapters-api and nb-annotations  modules contain all of the base classes, interfaces and annotations required to implement a new adapter and flesh out its functionality.

* Adapters - Every new adapter will, of course, include an Adapter class. This class will be a descendent of the [BaseDriverAdapter](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapters-api/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/BaseDriverAdapter.java#L34) class found here. NoSQLBench uses the Java Service Provider Interface(SPI) to define service providers. If you don't know what that is, don't worry about it now. The tl;dr is that extending the BaseDriverAdapter and annotating your driver adapter class with the Service annotation will result in that class being added to META-INF/services under the specified class name, making it available as a service provider. NoSQLBench takes care of all the rest!
* Ops - NoSQLBench is built around the ability to define operations ("Ops") in a generic fashion (the op template) and translate them into actions. All of the basic plumbing for this is handled in the adapters-api module, from the yaml file loader to the definition of the various types of ops available to implement. Along with the Op types the adapters-api package defines the base classes and interfaces for Op Dispensers, which do the work of creating the Op, and the Op Mappers, which map the appropriate functionality based on the Op type presented. We're going to get into the details of the available implementations in the next section.

# Your First Adapter

1. Familiarize yourself with the native driver. You will need to know exactly what functionality you want to test and what the APIs for that functionality look like so you can plan for what your Op classes need to be able to do.
2. Create a new module in the root of the project using standard naming:
   * adapter-*\<typeName>* where the chosen selector that will be used with driver=*\<typeName>*, e.g. adapter-jdbc, adapter-pinecone, etc.
   * package naming should follow io.nosqlbench.adapter.*\<typeName>*
   * A quick way to create module is to copy an existing adapter's pom.xml into a new directory as pod.xml, modify the naming elements, and then rename pod.xml to pom.xml
   * In most cases the only dependencies you should need are adapters-api and nb-annotations (as discussed in the previous section), as well as the api library published for the native driver you are adapting. If you find that you need to pull in additional dependencies, first verify whether they are included in another module already within the nb project to simplify the process.
   * Initially, do not add the module to the root pom.xml under modules. You can still build it and test it without requiring it to build for the main module to build. Once it is ready to be included under the main build, then you add it to the modules list. At that time, add it to the list of driver dependencies for the nb5 module, and it will be included in the runtime.
3. Implement your first Op.
   * Implement a POJO which implements one of the Op interfaces and represents a value type for a specific operation.
   * It is recommended to use [RunnableOp](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapters-api/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/flowtypes/RunnableOp.java#L20) by default. This means you only need to provide a run method to define whatever action this operation encapsulates. The op class itself should be extremely lightweight, as the logic for constructing the operation will take place in the dispenser.
   * As a value type it must be repeatable.
   * It should capture the details of a single operation for diagnostics and debugging purposes. In database terminology, for example, this might be "insert", or "delete", or "update".
4. Implement a minimal Adapter Space.
   * Stub a POJO which can hold instances of your native driver. This is your context, or adapter space.
   * This class should contain any logic needed to establish connectivity as well as any type of initialization required for the native driver.
   * The constructor of the adapter space should take a String and an [NBConfiguration](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/nb-api/src/main/java/io/nosqlbench/api/config/standard/NBConfiguration.java#L26) instance as its parameters. Any variables needed to initialize the environment should be accessible to the adapter space through the NBConfiguration instance. The adapter space class must define what these variables are by implementing the static getConfigModel() method. See [existing adapter space implementations](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapter-tcp/src/main/java/io/nosqlbench/adapter/tcpserver/TcpServerAdapterSpace.java#L43) for details on this.
5. Implement the DriverAdapter.
   * This class must extend the [BaseDriverAdapter](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapters-api/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/BaseDriverAdapter.java#L34) class. As the BaseDriverAdapter class is a templated type, this implementation should use the 2 classes previously created, i.e.
   ```MyDriverAdapter extends BaseDriverAdapter<MyOp, MyAdapterSpace>```
   * Add the @Service annotation that makes it available for runtime service looking and late binding.
   ```@Service(value = DriverAdapter.class, selector = "nativedrivertype")```
   * Minimally this class must implement 2 methods, both of which should specify that they are overriding the base class
     * getOpMapper - this method will return the OpMapper specific to the types of Op classes that will be created for this driver. As we have not yet implemented this class, for now it can be stubbed to simply return null. The signature should look like
     ```public OpMapper<MyOp> getOpMapper()```
     * getSpaceInitializer - this method accepts an [NBConfiguration](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/nb-api/src/main/java/io/nosqlbench/api/config/standard/NBConfiguration.java#L26) instance as an argument and will return a function that can be used to instantiate the previously defined adapter space. In its simplest form it will simply pass the configuration along to the constructor for the adapter space. The signature should look like
     ```public Function<String, ? extends MyAdapterSpace> getSpaceInitializer(NBConfiguration cfg)```
6. Implement an OpDispenser for this Op type.
   * This must extend [BaseOpDispenser](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapters-api/src/main/java/io/nosqlbench/engine/api/activityimpl/BaseOpDispenser.java#L43), which is a templated class. The definition might look like this:
   ```public class MyOpDispenser extends BaseOpDispenser<MyOp, MyAdapterSpace>```
   * The constructor must accept at least a DriverAdapter and an Op of the specified type, and it must call the super constructor, which requires the DriverAdapter and Op to be provided.
   * The constructor for this class will need at least the function for retrieving the relevant Adapter Space to facilitate the creation of the Op to be dispensed, and a ParsedOp object which is the operation as derived from the source yaml/JSON file to be converted to the appropriate op type by the dispenser.
   * The OpDispenser must also override the apply method defined in the BaseOpDispenser. For each test cycle this apply method will be called, and the OpDispenser will need to return the created Op for that cycle. The typical pattern used in the implementation of the OpDispenser is that at the time of construction it defines a LongFunction to create the Op that is dispensed when the apply method is called. The apply method itself is minimal and applies the input value (the cycle) to this function and dispenses the Op returned.
   * This is another place where it should be noted that in an adapter of any complexity there will usually be multiple Op types, each of which will have its own OpDispenser class responsible for dispensing only that type of Op. Don't implement a hierarchy of Op types and dispense them through a single OpDispenser via dynamic binding.
7. Implement an OpMapper to create the OpDispenser
   * The OpMapper has a very simple job in the case of having only a single OpDispenser type, it creates the OpDispenser. In more complicated use cases the OpMapper receives an Op and interrogates it to determine the appropriate OpDispenser to create. More on this below.
   * Your class should implement the [OpMapper](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapters-api/src/main/java/io/nosqlbench/engine/api/activityimpl/OpMapper.java#L27)<T> interface, with the templated type once again being your Op. It then needs to override the apply(ParsedOp) method to return the OpDispenser. The signature should look something like this:
   ```public OpDispenser<? extends MyOp> apply(ParsedOp op)```
   * At this point you can treat the apply method largely as a pass-through and simply return a new OpDispenser instance (although take a look at the existing implementations, you will probably want to emulate the use of the space cache to pass the dispenser the space object as well).
   * The constructor for this class doesn't have any hard requirements, but if you look at the existing implementations you will notice most of them include as an argument a new class we haven't talked about yet, the [DriverSpaceCache](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapters-api/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/DriverSpaceCache.java#L25). Not to worry, this is simply a cache to hold your associated context, the details of which are handled once again by the NoSQLBench plumbing. In the next step we will look at how this is used in the Adapter class.
8. Wire your classes together and get it to compile cleanly.
   * Go back to the Adapter type created in step 5. Now you're going to fill in that getOpMapper method. If your code looks like the majority of implementations the method body will look like this:

   ```
   DriverSpaceCache<? extends MyAdapterSpace> spaceCache = getSpaceCache();
   NBConfiguration config = getConfiguration();
   return new MyOpMapper(this, config, spaceCache);
   ```
   * As pointed out earlier this "just works" because NoSQLBench already provides the plumbing behind these calls. The [BaseDriverAdapter](https://github.com/nosqlbench/nosqlbench/blob/208296f68f97e06a194e2155229c0a0f3c69059b/adapters-api/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/BaseDriverAdapter.java#L34) handles the necessary initialization and storage of the appropriate type of space cache and NB configuration, and these can simply be passed in to the constructor for your OpMapper class!
   * You should now have:
     * An op class that implements the RunnableOp interface and defines a run method that does something specific to the native driver you are working with.
     * An adapter space class that encapsulates the context of the native driver and implements any logic necessary for initialization.
     * A driver adapter class that contains the logic to instantiate both the op mapper class and the adapter space class.
     * An op dispenser class that both contains the logic to construct instances of your op type from a ParsedOp object and implements the apply method to return new instances of your op type.
     * An op mapper class, as returned by the driver adapter, that implements the apply method to accept a ParsedOp instance and return an instance of the previously defined op dispenser class.

# Further Reading
## Use Cases With Multiple Op Types

In most sophisticated use cases a single op type will not be sufficient. As in the canonical example of a database where a user may want to perform a number of different operations such as inserting new records, reading existing records, updating, deleting, etc. In these cases it is necessary to define more than a single Op class, defining a separate class for each of these operations. In these cases there should be a separate OpDispenser class defined for each Op type as well, with the OpMapper class creating the OpDispenser type appropriate for the operation being performed.

The Op classes should remain as compact as possible, implementing only the basic functionality they are ascribed. The OpDispenser classes remain similar in structure, with each creating a function specific to the Op type it is associated with, to be called by the apply method at the time when the Op needs to be created. The difference in implementation is largely confined to how the OpDispenser class interacts with the ParsedOp object it receives in its constructor. The ParsedOp represents a single operation as defined by the source configuration yaml file passed at runtime. The yaml might contain any arbitrary number of different ops, each of which will be interpreted and result in a ParsedOp to be passed to the Mapper and Dispenser at runtime. Each dispenser can query the ParsedOp for the existence of the fields it expects to find defined for instantiation of the Op type it is responsible for, and define the creation functionality based on what it finds to be present.

In the cases where multiple Op types are defined an OpType enum class should also be provided. This allows the OpMapper class to use the TypeAndTarget functionality provided by the ParsedOp API. getTypeAndTarget is a method exposed by the ParsedOp API which allows the caller to pass in as arguments the class of the enum, the expected class the resulting function should return, the type name and the value name and in return receive a TypeAndTarget Object containing the enum type identification and a target function that will return the value associated with the type. This target function can be thought of as providing the "key" for the Op type in question. For example in the pinecone adapter every operation needs to specify the database index the operation will run against. This is represented in the op definition as:

    type: [query,delete,update…]
    index: <database index>.

And the call to TypeAndTarget is:

	op.getTypeAndTarget(PineconeOpTypes.class, String.class, "type", "index").

With this definition the target function will return the value of the database index for the given Op.
