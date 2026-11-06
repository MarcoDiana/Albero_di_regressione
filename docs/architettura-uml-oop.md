# Diagrammi UML e Architettura dei Package

## 1. Architettura dei Package e Dipendenze («import»)

```mermaid
flowchart TD
    subgraph ClientModule ["Modulo Client (Maven)"]
        PkgClient["Client"]
        PkgUtility["utility"]
        PkgClientDto["dto"]
    end

    subgraph ServerModule ["Modulo Server (Maven)"]
        PkgServer["server"]
        PkgTree["tree"]
        PkgData["data"]
        PkgDatabase["database"]
        PkgServerDto["dto"]
    end

    PkgClient -.->|"«import»"| PkgUtility
    PkgClient -.->|"«import»"| PkgClientDto
    PkgServer -.->|"«import»"| PkgTree
    PkgServer -.->|"«import»"| PkgData
    PkgServer -.->|"«import»"| PkgDatabase
    PkgServer -.->|"«import»"| PkgServerDto
    PkgTree -.->|"«import»"| PkgData
    PkgTree -.->|"«import»"| PkgServerDto
    PkgTree -.->|"«import»"| PkgServer
    PkgData -.->|"«import»"| PkgDatabase
```

## 2. Diagramma UML delle Classi — Package `data`

```mermaid
classDiagram
    class Attribute {
        <<abstract>>
        -long serialVersionUID$
        -String name
        -int index
        #Attribute(name: String, index: int)
        +getName() String
        +getIndex() int
        +toString() String
    }

    class ContinuousAttribute {
        -long serialVersionUID$
        +ContinuousAttribute(name: String, index: int)
    }

    class DiscreteAttribute {
        -long serialVersionUID$
        -Set~String~ values
        +DiscreteAttribute(name: String, index: int, values: String[])
        +getNumberOfDistinctValues() int
        +iterator() Iterator~String~
    }

    class Data {
        -List~Example~ data
        -int numberOfExamples
        -List~Attribute~ explanatorySet
        -ContinuousAttribute classAttribute
        +Data(tableName: String)
        -isNumericType(sqlType: int) boolean
        +getNumberOfExamples() int
        +getNumberOfExplanatoryAttributes() int
        +getClassValue(exampleIndex: int) Double
        +getExplanatoryValue(exampleIndex: int, attributeIndex: int) Object
        +getExplanatoryAttribute(index: int) Attribute
        +getClassAttribute() ContinuousAttribute
        +toString() String
        +sort(attribute: Attribute, beginExampleIndex: int, endExampleIndex: int) void
        -swap(i: int, j: int) void
        -partition(attribute: DiscreteAttribute, inf: int, sup: int) int
        -partition(attribute: ContinuousAttribute, inf: int, sup: int) int
        -quicksort(attribute: Attribute, inf: int, sup: int) void
        +main(args: String[])$ void
    }

    class TrainingDataException {
        -long serialVersionUID$
        +TrainingDataException(message: String)
        +TrainingDataException(message: String, cause: Throwable)
    }

    Attribute <|-- ContinuousAttribute
    Attribute <|-- DiscreteAttribute
    Iterable <|.. DiscreteAttribute
    Serializable <|.. Attribute
    Exception <|-- TrainingDataException
    Data *-- "0..*" Example : data
    Data *-- "1..*" Attribute : explanatorySet
    Data *-- "1" ContinuousAttribute : classAttribute
```

## 3. Diagramma UML delle Classi — Package `database`

```mermaid
classDiagram
    class DbAccess {
        -String DRIVER_CLASS_NAME$
        -String DBMS$
        -int PORT$
        -String SERVER
        -String DATABASE
        -String USER_ID
        -String PASSWORD
        -Connection conn
        +DbAccess()
        +initConnection() void
        +getConn() Connection
        +getConnection() Connection
        +closeConnection() void
    }

    class Column {
        -String name
        -String type
        ~Column(name: String, type: String)
        +getColumnName() String
        +isNumber() boolean
        +toString() String
    }

    class Example {
        -List~Object~ example
        +Example()
        +add(o: Object) void
        +get(i: int) Object
        +compareTo(ex: Example) int
        +toString() String
        +iterator() Iterator~Object~
    }

    class TableSchema {
        -List~Column~ tableSchema
        +TableSchema(db: DbAccess, tableName: String)
        +getNumberOfAttributes() int
        +getColumn(index: int) Column
        +iterator() Iterator~Column~
    }

    class TableData {
        -DbAccess db
        +TableData(db: DbAccess)
        +getTransazioni(table: String) List~Example~
        +getDistinctColumnValues(table: String, column: Column) Set~Object~
    }

    class DatabaseConnectionException {
        -long serialVersionUID$
        +DatabaseConnectionException(message: String)
        +DatabaseConnectionException(message: String, cause: Throwable)
    }

    class EmptySetException {
        -long serialVersionUID$
        +EmptySetException(message: String)
        +EmptySetException(message: String, cause: Throwable)
    }

    Iterable <|.. Example
    Comparable <|.. Example
    Iterable <|.. TableSchema
    Exception <|-- DatabaseConnectionException
    Exception <|-- EmptySetException
    TableSchema *-- "1..*" Column : tableSchema
    TableData o-- "1" DbAccess : db
```

## 4. Diagramma UML delle Classi — Package `tree`

```mermaid
classDiagram
    class Node {
        <<abstract>>
        -long serialVersionUID$
        -int idNodeCount$
        -int idNode
        -int beginExampleIndex
        -int endExampleIndex
        -double variance
        ~Node(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int)
        ~getIdNode() int
        ~getBeginExampleIndex() int
        ~getEndExampleIndex() int
        ~getVariance() double
        ~getNumberOfChildren()* int
        +toString() String
    }

    class LeafNode {
        -long serialVersionUID$
        -Double predictedClassValue
        ~LeafNode(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int)
        ~getPredictedClassValue() Double
        ~getNumberOfChildren() int
        +toString() String
    }

    class SplitNode {
        <<abstract>>
        -long serialVersionUID$
        -Attribute attribute
        #List~SplitInfo~ mapSplit
        -double splitVariance
        ~SplitNode(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: Attribute)
        #setSplitInfo(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: Attribute)* void
        ~testCondition(value: Object)* int
        ~getAttribute() Attribute
        ~getVariance() double
        ~getNumberOfChildren() int
        ~getSplitInfo(child: int) SplitInfo
        ~formulateQuery() String
        +compareTo(o: SplitNode) int
        +toString() String
    }

    class SplitInfo {
        -long serialVersionUID$
        -Object splitValue
        -int beginIndex
        -int endIndex
        -int numberChild
        -String comparator
        ~SplitInfo(splitValue: Object, beginIndex: int, endIndex: int, numberChild: int)
        ~SplitInfo(splitValue: Object, beginIndex: int, endIndex: int, numberChild: int, comparator: String)
        ~getBeginIndex() int
        ~getEndIndex() int
        ~getSplitValue() Object
        ~getNumberChild() int
        ~getComparator() String
        +toString() String
    }

    class DiscreteNode {
        -long serialVersionUID$
        ~DiscreteNode(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: DiscreteAttribute)
        #setSplitInfo(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: Attribute) void
        ~testCondition(value: Object) int
        +toString() String
    }

    class ContinuousNode {
        -long serialVersionUID$
        ~ContinuousNode(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: ContinuousAttribute)
        #setSplitInfo(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: Attribute) void
        ~testCondition(value: Object) int
        +toString() String
    }

    class RegressionTree {
        -long serialVersionUID$
        -Node root
        -RegressionTree[] childTree
        -RegressionTree()
        +RegressionTree(trainingSet: Data)
        -learnTree(trainingSet: Data, begin: int, end: int, numberOfExamplesPerLeaf: int) void
        -isLeaf(trainingSet: Data, begin: int, end: int, numberOfExamplesPerLeaf: int) boolean
        -determineBestSplitNode(trainingSet: Data, begin: int, end: int) SplitNode
        +printRules() void
        -printRules(current: String) void
        +getRules() String
        +printTree() void
        +toString() String
        +predictClass(out: ObjectOutputStream, in: ObjectInputStream) Double
        +navigate(path: List~Integer~) PredictionStepDto
        +salva(nomeFile: String) void
        +carica(nomeFile: String)$ RegressionTree
    }

    Node <|-- LeafNode
    Node <|-- SplitNode
    SplitNode <|-- DiscreteNode
    SplitNode <|-- ContinuousNode
    Comparable <|.. SplitNode
    Serializable <|.. Node
    Serializable <|.. SplitInfo
    Serializable <|.. RegressionTree
    RegressionTree *-- "1" Node : root
    RegressionTree *-- "0..*" RegressionTree : childTree
    SplitNode *-- "1..*" SplitInfo : mapSplit
    SplitNode o-- "1" Attribute : attribute
```

## 5. Diagramma UML delle Classi — Package `server`

```mermaid
classDiagram
    class ServerApplication {
        +ServerApplication()
        +main(args: String[])$ void
    }

    class TreeService {
        -Map~String,RegressionTree~ activeTrees
        +TreeService()
        +learnFromDb(tableName: String) TreeResponseDto
        +loadFromFile(tableName: String) TreeResponseDto
        +predictStep(tableName: String, path: List~Integer~) PredictionStepDto
        +clearCache() void
    }

    class TreeController {
        -TreeService treeService
        +TreeController(treeService: TreeService)
        +learn(request: TreeRequestDto) ResponseEntity~TreeResponseDto~
        +load(request: TreeRequestDto) ResponseEntity~TreeResponseDto~
        +predict(request: PredictRequestDto) ResponseEntity~PredictionStepDto~
    }

    class GlobalExceptionHandler {
        +GlobalExceptionHandler()
        +handleFileNotFound(ex: FileNotFoundException) ResponseEntity
        +handleDomainExceptions(ex: Exception) ResponseEntity
        +handleIoAndFormatExceptions(ex: Exception) ResponseEntity
        +handleGenericException(ex: Exception) ResponseEntity
    }

    class MultiServer {
        -int port
        +MultiServer(port: int)
        -run() void
    }

    class ServerOneClient {
        -Socket socket
        -ObjectInputStream in
        -ObjectOutputStream out
        +ServerOneClient(s: Socket)
        +run() void
    }

    class UnknownValueException {
        -long serialVersionUID$
        +UnknownValueException(message: String)
        +UnknownValueException(message: String, cause: Throwable)
    }

    Thread <|-- ServerOneClient
    Exception <|-- UnknownValueException
    TreeController --> TreeService : delega business logic
    TreeService o-- "0..*" RegressionTree : cache activeTrees
    MultiServer ..> ServerOneClient : crea thread
    ServerOneClient ..> RegressionTree : esegue
```

## 6. Diagramma UML delle Classi — Package `Client` e `utility`

```mermaid
classDiagram
    class ClientApplication {
        +ClientApplication()
        +main(args: String[])$ void
    }

    class Main {
        +Main()
        +main(args: String[])$ void
    }

    class TreeRestClientService {
        -ObjectMapper OBJECT_MAPPER$
        -RestClient restClient
        +TreeRestClientService()
        +TreeRestClientService(restClientBuilder: Builder, baseUrl: String)
        +TreeRestClientService(baseUrl: String)
        +learnTree(tableName: String) TreeResponseDto
        +loadTree(tableName: String) TreeResponseDto
        +predictStep(tableName: String, path: List~Integer~) PredictionStepDto
    }

    class MainView {
        -TreeRestClientService treeRestClientService
        -String currentTableName
        -List~Integer~ currentPath
        -TextField tableNameField
        -Button learnBtn
        -Button loadBtn
        -TextArea rulesArea
        -VerticalLayout stepContainer
        +MainView(treeRestClientService: TreeRestClientService)
        -handleLearn() void
        -handleLoad() void
        -renderInitialStepState() void
        -doPredictStep() void
        -renderStep(step: PredictionStepDto) void
        -showSuccess(msg: String) void
        -showError(msg: String) void
    }

    class Keyboard {
        -boolean printErrors$
        -int errorCount$
        -String current_token$
        -StringTokenizer reader$
        -BufferedReader in$
        -Keyboard()
        +getErrorCount()$ int
        +resetErrorCount(count: int)$ void
        +getPrintErrors()$ boolean
        +setPrintErrors(flag: boolean)$ void
        -error(str: String)$ void
        -getNextToken()$ String
        -getNextToken(skip: boolean)$ String
        -getNextInputToken(skip: boolean)$ String
        +endOfLine()$ boolean
        +readString()$ String
        +readWord()$ String
        +readBoolean()$ boolean
        +readChar()$ char
        +readInt()$ int
        +readLong()$ long
        +readFloat()$ float
        +readDouble()$ double
    }

    AppShellConfigurator <|.. ClientApplication
    VerticalLayout <|-- MainView
    MainView --> TreeRestClientService : chiama REST
    Main ..> Keyboard : I/O console
```

## 7. Diagramma UML delle Classi — Package `dto` (Contratti JSON)

```mermaid
classDiagram
    class TreeRequestDto {
        <<record>>
        -long serialVersionUID$
        -String tableName
        +tableName() String
    }

    class PredictionStepDto {
        <<record>>
        -long serialVersionUID$
        -boolean isLeaf
        -Double predictedValue
        -String attributeName
        -List~String~ branchOptions
        +isLeaf() boolean
        +predictedValue() Double
        +attributeName() String
        +branchOptions() List~String~
    }

    class TreeResponseDto {
        <<record>>
        -long serialVersionUID$
        -String tableName
        -String treeRules
        -PredictionStepDto rootStep
        +tableName() String
        +treeRules() String
        +rootStep() PredictionStepDto
    }

    class PredictRequestDto {
        <<record>>
        -long serialVersionUID$
        -String tableName
        -List~Integer~ path
        +tableName() String
        +path() List~Integer~
    }

    Serializable <|.. TreeRequestDto
    Serializable <|.. PredictionStepDto
    Serializable <|.. TreeResponseDto
    Serializable <|.. PredictRequestDto
    TreeResponseDto o-- "1" PredictionStepDto : rootStep
```

## 8. Diagramma di Sequenza 1: Apprendimento e Navigazione REST Stateless (Spring Boot 8080)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client HTTP / cURL
    participant Controller as TreeController (8080)
    participant Service as TreeService
    participant DB as MySQL MapDB
    participant Tree as RegressionTree
    participant Disk as File System (.dmp)

    Client->>Controller: POST /api/trees/learn {"tableName": "provaC"}
    Controller->>Service: learnFromDb("provaC")
    Service->>DB: Query dati tabella 'provaC'
    DB-->>Service: Recordset tuple di training
    Service->>Tree: new RegressionTree(data)
    Service->>Disk: tree.salva("provaC.dmp")
    Service->>Service: activeTrees.put("provaC", tree)
    Service->>Tree: tree.navigate([])
    Tree-->>Service: PredictionStepDto (radice: attributeName, branchOptions)
    Service-->>Controller: TreeResponseDto ("provaC", treeRules, rootStep)
    Controller-->>Client: 200 OK (TreeResponseDto JSON)

    Client->>Controller: POST /api/trees/predict {"tableName": "provaC", "path": [1]}
    Controller->>Service: predictStep("provaC", [1])
    Service->>Service: activeTrees.get("provaC")
    Service->>Tree: tree.navigate([1])
    Tree-->>Service: PredictionStepDto (isLeaf=true, predictedValue=42.5)
    Service-->>Controller: PredictionStepDto
    Controller-->>Client: 200 OK (PredictionStepDto JSON)
```

## 9. Diagramma di Sequenza 2: Interazione Web UI Vaadin Flow (Porta 8081)

```mermaid
sequenceDiagram
    autonumber
    actor Utente as Utente Web
    participant UI as MainView (Vaadin 8081)
    participant RESTClient as TreeRestClientService
    participant Backend as REST Controller (8080)

    Utente->>UI: Inserisce "provaC" in tableNameField e clicca learnBtn
    UI->>RESTClient: learnTree("provaC")
    RESTClient->>Backend: POST /api/trees/learn {"tableName": "provaC"}
    Backend-->>RESTClient: 200 OK (TreeResponseDto)
    RESTClient-->>UI: TreeResponseDto
    UI->>UI: rulesArea.setValue(treeRules)
    UI->>UI: renderStep(rootStep) -> popola pulsanti opzioni split
    UI->>Utente: Mostra regole e rami del nodo radice

    Utente->>UI: Clicca pulsante ramo (es. indice 1)
    UI->>UI: currentPath.add(1)
    UI->>RESTClient: predictStep("provaC", [1])
    RESTClient->>Backend: POST /api/trees/predict {"tableName": "provaC", "path": [1]}
    Backend-->>RESTClient: 200 OK (PredictionStepDto)
    RESTClient-->>UI: PredictionStepDto
    UI->>UI: renderStep(step) -> rileva isLeaf == true
    UI->>Utente: Visualizza badge risultato predetto e riepilogo
```

## 10. Diagramma di Sequenza 3: Protocollo Socket TCP Legacy (CLI - Porta 8080)

```mermaid
sequenceDiagram
    autonumber
    actor Utente as Utente Console
    participant CLI as Main (Client CLI)
    participant KB as Keyboard
    participant Server as ServerOneClient (8080)
    participant DB as MySQL MapDB
    participant Tree as RegressionTree
    participant Disk as File System (.dmp)

    Utente->>KB: readInt() -> Scelta 1 (Apprendi da DB)
    Utente->>KB: readString() -> tableName "provaC"
    CLI->>Server: out.writeObject(0) [comando acquisizione]
    CLI->>Server: out.writeObject("provaC")
    Server->>DB: Query dati tabella 'provaC'
    DB-->>Server: Righe del training set
    Server-->>CLI: in.readObject() -> "OK"

    CLI->>Server: out.writeObject(1) [comando apprendimento]
    Server->>Tree: new RegressionTree(data)
    Server->>Disk: tree.salva("provaC.dmp")
    Server-->>CLI: in.readObject() -> "OK"

    Utente->>KB: readInt() -> Scelta 3 (Predizione)
    CLI->>Server: out.writeObject(3) [comando predizione]
    Server->>Tree: tree.predictClass(out, in)
    loop Fino a LeafNode
        Tree-->>CLI: in.readObject() -> stringa condizioni split
        CLI->>Utente: Visualizza query e opzioni del ramo
        Utente->>KB: readInt() -> inserisce indice ramo
        CLI->>Tree: out.writeObject(ramoScelto)
    end
    Tree-->>Server: return Double predictedValue
    Server-->>CLI: out.writeObject("OK")
    Server-->>CLI: out.writeObject(predictedValue)
    CLI->>Utente: Visualizza valore predetto a console
```

## 11. Diagramma di Deployment / Componenti (Porte 8080, 8081, 3306 MySQL MapDB)

```mermaid
flowchart TB
    subgraph ClientHost ["Client Tier (Host Utente / Frontend)"]
        Browser["Web Browser (UI Vaadin Flow)<br/>http://localhost:8081"]
        ClientApp["Spring Boot Web Client (Porta 8081)<br/>ClientApplication, MainView, TreeRestClientService"]
        CLIClient["Java CLI Client (Console)<br/>Main, Keyboard"]
    end

    subgraph ServerHost ["Logic Tier (Server Host / Backend)"]
        direction TB
        Tomcat["Spring Boot Embedded Tomcat (Porta 8080)<br/>ServerApplication, TreeController, TreeService, GlobalExceptionHandler"]
        SocketServer["MultiServer Socket TCP (Porta 8080)<br/>MultiServer, ServerOneClient"]
        TreeEngine["Regression Tree Engine<br/>RegressionTree, Data, DbAccess, TableData, TableSchema"]
        Tomcat --> TreeEngine
        SocketServer --> TreeEngine
    end

    subgraph DataHost ["Data Tier (Storage Host / Database)"]
        MySQL["MySQL Database Server 8.x (Porta 3306)<br/>Database: MapDB | Utente: MapUser"]
        Disk["File System Locale<br/>Archivi serializzati (*.dmp)"]
    end

    Browser -->|HTTP / WebSocket :8081| ClientApp
    ClientApp -->|HTTP REST POST / JSON :8080| Tomcat
    CLIClient -->|TCP Socket ObjectStreams :8080| SocketServer
    TreeEngine -->|JDBC Driver com.mysql.cj.jdbc.Driver :3306| MySQL
    TreeEngine -->|File I/O ObjectStreams| Disk
    SocketServer -->|File I/O ObjectStreams| Disk
```
