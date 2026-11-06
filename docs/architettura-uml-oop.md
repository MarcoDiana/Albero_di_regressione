# Diagrammi UML e Architettura dei Package

## 1. Architettura dei Package e Dipendenze («import»)

```mermaid
flowchart TD
    subgraph ClientLayer ["Client Layer"]
        PkgClient["Client"]
        PkgUtility["utility"]
    end

    subgraph DtoLayer ["Shared Contracts"]
        PkgDto["dto"]
    end

    subgraph ServerLayer ["Server Layer"]
        PkgServer["server"]
        PkgTree["tree"]
        PkgData["data"]
        PkgDatabase["database"]
    end

    PkgClient -.->|"«import»"| PkgUtility
    PkgClient -.->|"«import»"| PkgDto
    PkgServer -.->|"«import»"| PkgTree
    PkgServer -.->|"«import»"| PkgData
    PkgServer -.->|"«import»"| PkgDatabase
    PkgServer -.->|"«import»"| PkgDto
    PkgTree -.->|"«import»"| PkgData
    PkgData -.->|"«import»"| PkgDatabase
```

## 2. Diagramma UML delle Classi — Package `data`

```mermaid
classDiagram
    class Attribute {
        <<abstract>>
        -String name
        -int index
        #Attribute(name: String, index: int)
        +getName() String
        +getIndex() int
        +toString() String
    }

    class ContinuousAttribute {
        +ContinuousAttribute(name: String, index: int)
    }

    class DiscreteAttribute {
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
        -String DRIVER_CLASS_NAME
        -String DBMS
        -String SERVER
        -String DATABASE
        -int PORT
        -String USER_ID
        -String PASSWORD
        -Connection conn
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
        +add(o: Object) void
        +get(i: int) Object
        +compareTo(ex: Example) int
        +iterator() Iterator~Object~
        +toString() String
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
        +DatabaseConnectionException(message: String)
    }

    class EmptySetException {
        +EmptySetException(message: String)
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
        -Double predictedClassValue
        ~LeafNode(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int)
        ~getPredictedClassValue() Double
        ~getNumberOfChildren() int
        +toString() String
    }

    class SplitNode {
        <<abstract>>
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
        ~DiscreteNode(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: DiscreteAttribute)
        #setSplitInfo(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: Attribute) void
        ~testCondition(value: Object) int
        +toString() String
    }

    class ContinuousNode {
        ~ContinuousNode(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: ContinuousAttribute)
        #setSplitInfo(trainingSet: Data, beginExampleIndex: int, endExampleIndex: int, attribute: Attribute) void
        ~testCondition(value: Object) int
        +toString() String
    }

    class RegressionTree {
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

## 5. Diagramma UML delle Classi — Package `server`, `Client` e `utility`

```mermaid
classDiagram
    class MultiServer {
        -int PORT
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
        +UnknownValueException(message: String)
    }

    class ClientMain {
        +main(args: String[])$ void
    }

    class Keyboard {
        -boolean printErrors$
        -int errorCount$
        -String current_token$
        -StringTokenizer reader$
        -BufferedReader in$
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

    Thread <|-- ServerOneClient
    Exception <|-- UnknownValueException
    MultiServer ..> ServerOneClient : crea thread
    ServerOneClient ..> RegressionTree : esegue
    ClientMain ..> Keyboard : I/O console
```

## 6. Diagramma UML delle Classi — Package `dto` (Contratti JSON)

```mermaid
classDiagram
    class TreeRequestDto {
        <<record>>
        -String tableName
        +tableName() String
    }

    class PredictionStepDto {
        <<record>>
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
        -String tableName
        -String treeRules
        -PredictionStepDto rootStep
        +tableName() String
        +treeRules() String
        +rootStep() PredictionStepDto
    }

    class PredictRequestDto {
        <<record>>
        -String tableName
        -List~Integer~ path
        +tableName() String
        +path() List~Integer~
    }

    Serializable <|.. TreeRequestDto
    Serializable <|.. PredictionStepDto
    Serializable <|.. TreeResponseDto
    Serializable <|.. PredictRequestDto
    TreeResponseDto o-- PredictionStepDto : rootStep
```

## 7. Diagramma UML delle Classi — Componenti Spring Boot REST e Vaadin Flow

```mermaid
classDiagram
    class ServerApplication {
        +main(args: String[])$ void
    }

    class TreeService {
        -Map~String,RegressionTree~ activeTrees
        +learnFromDb(tableName: String) TreeResponseDto
        +loadFromFile(tableName: String) TreeResponseDto
        +predictStep(tableName: String, path: List~Integer~) PredictionStepDto
    }

    class TreeController {
        -TreeService treeService
        +TreeController(treeService: TreeService)
        +learn(request: TreeRequestDto) ResponseEntity
        +load(request: TreeRequestDto) ResponseEntity
        +predict(request: PredictRequestDto) ResponseEntity
    }

    class GlobalExceptionHandler {
        +handleNotFound(ex: Exception) ResponseEntity
        +handleBadRequest(ex: Exception) ResponseEntity
    }

    class ClientApplication {
        +main(args: String[])$ void
    }

    class TreeRestClientService {
        -RestClient restClient
        +TreeRestClientService(builder: RestClient.Builder)
        +learnTree(tableName: String) TreeResponseDto
        +loadTree(tableName: String) TreeResponseDto
        +predictStep(tableName: String, path: List~Integer~) PredictionStepDto
    }

    class MainView {
        -TreeRestClientService service
        -TextField tableField
        -TextArea rulesArea
        -VerticalLayout predictLayout
        -List~Integer~ currentPath
        +MainView(service: TreeRestClientService)
        -learnAction() void
        -loadAction() void
        -updatePredictionView(step: PredictionStepDto) void
    }

    TreeController --> TreeService : delega business logic
    TreeService o-- "0..*" RegressionTree : cache activeTrees
    MainView --> TreeRestClientService : chiama REST
    TreeRestClientService ..> TreeController : HTTP POST JSON
    VerticalLayout <|-- MainView
```

## 8. Diagramma UML Panoramico delle Relazioni tra Classi Principali

```mermaid
classDiagram
    class RegressionTree {
        -Node root
        -RegressionTree[] childTree
        +RegressionTree(trainingSet: Data)
        +predictClass(out, in) Double
        +navigate(path) PredictionStepDto
    }

    class Node {
        <<abstract>>
        -double variance
        ~getNumberOfChildren()* int
    }

    class LeafNode {
        -Double predictedClassValue
    }

    class SplitNode {
        <<abstract>>
        -Attribute attribute
        #List~SplitInfo~ mapSplit
    }

    class SplitInfo {
        -Object splitValue
        -String comparator
    }

    class Data {
        -List~Example~ data
        -List~Attribute~ explanatorySet
        -ContinuousAttribute classAttribute
    }

    class Attribute {
        <<abstract>>
        -String name
    }

    class DbAccess {
        -Connection conn
        +initConnection() void
    }

    class TableData {
        +getTransazioni(table) List
    }

    class ServerOneClient {
        +run() void
    }

    class MainView {
        +MainView(service)
    }

    class TreeController {
        +predict(request) ResponseEntity
    }

    Node <|-- LeafNode
    Node <|-- SplitNode
    SplitNode *-- "1..*" SplitInfo : mapSplit
    SplitNode o-- "1" Attribute : attribute
    RegressionTree *-- "1" Node : root
    RegressionTree *-- "0..*" RegressionTree : childTree
    Data *-- "1..*" Attribute : explanatorySet
    TableData o-- "1" DbAccess : db
    ServerOneClient ..> RegressionTree : uses
    MainView ..> TreeController : REST HTTP
    TreeController ..> RegressionTree : navigates
```

## 9. Diagramma di Sequenza UML — Protocollo Socket Multi-Client

```mermaid
sequenceDiagram
    autonumber
    actor Utente
    participant Client as Client Main
    participant Server as ServerOneClient
    participant Tree as RegressionTree
    participant DB as DbAccess / MySQL

    Utente->>Client: Scelta 1: Apprendi da DB
    Client->>Server: Invia comando 0 + tableName
    Server->>DB: SELECT * FROM tableName
    DB-->>Server: Righe del training set
    Server-->>Client: OK
    Client->>Server: Invia comando 1 (learn)
    Server->>Tree: new RegressionTree(data)
    Server->>Tree: tree.salva(tableName.dmp)
    Server-->>Client: OK
    Client->>Server: Invia comando 3 (predict)
    loop Fino a LeafNode
        Server->>Tree: predictClass(out, in)
        Tree-->>Client: QUERY (opzioni dello split)
        Client->>Utente: Mostra condizioni
        Utente->>Client: Inserisce ramo (es. 0 o 1)
        Client->>Tree: Invia indice del ramo
    end
    Tree-->>Server: Restituisce valore predetto
    Server-->>Client: OK + predictedValue
    Client->>Utente: Visualizza valore predetto
```

## 10. Diagramma di Sequenza UML — Protocollo REST Stateless (Spring Boot + Vaadin Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Utente
    participant UI as MainView (Vaadin 8081)
    participant RESTClient as TreeRestClientService
    participant Controller as TreeController (8080)
    participant Service as TreeService
    participant Tree as RegressionTree
    participant DB as MySQL MapDB

    Utente->>UI: Clicca 'Apprendi da Database' (tableName)
    UI->>RESTClient: learnTree(tableName)
    RESTClient->>Controller: POST /api/trees/learn (JSON)
    Controller->>Service: learnFromDb(tableName)
    Service->>DB: Query dati tabella
    DB-->>Service: Dati training set
    Service->>Tree: new RegressionTree(data)
    Service->>Tree: tree.navigate([]) -> rootStep
    Service-->>Controller: TreeResponseDto (rules + rootStep)
    Controller-->>RESTClient: 200 OK (JSON)
    RESTClient-->>UI: TreeResponseDto
    UI->>Utente: Mostra regole e pulsanti primo split

    Utente->>UI: Clicca ramo split (es. ramo 1)
    UI->>RESTClient: predictStep(tableName, [1])
    RESTClient->>Controller: POST /api/trees/predict (JSON)
    Controller->>Service: predictStep(tableName, [1])
    Service->>Tree: tree.navigate([1])
    Tree-->>Service: PredictionStepDto (isLeaf=true, value=1.5)
    Service-->>Controller: PredictionStepDto
    Controller-->>RESTClient: 200 OK (JSON)
    RESTClient-->>UI: PredictionStepDto
    UI->>Utente: Mostra badge risultato predetto
```

## 11. Diagramma di Deployment UML — Architettura Three-Tier

```mermaid
flowchart TB
    subgraph ClientHost ["Client Tier (Host Utente)"]
        Browser["Web Browser (Vaadin UI)<br/>http://localhost:8081"]
        CLI["Java Console Client<br/>(Main + Keyboard)"]
    end

    subgraph ServerHost ["Logic Tier (Server Host)"]
        direction TB
        Tomcat["Spring Boot Embedded Tomcat<br/>Porta 8080"]
        REST["REST API Controller<br/>/api/trees/* + Swagger UI"]
        Service["TreeService (In-Memory Cache)"]
        SocketServer["MultiServer Socket<br/>Porta 8080"]
        Tomcat --> REST --> Service
    end

    subgraph DataHost ["Data Tier (Database Host)"]
        MySQL["MySQL Database Server 8.x<br/>Porta 3306 (MapDB)"]
        Disk["File System Locale<br/>Archivi serializzati (*.dmp)"]
    end

    Browser -->|HTTP / JSON :8081| Tomcat
    CLI -->|TCP Socket :8080| SocketServer
    Service -->|JDBC Driver com.mysql.cj.jdbc.Driver| MySQL
    Service -->|File I/O ObjectStreams| Disk
    SocketServer -->|File I/O ObjectStreams| Disk
    SocketServer -->|JDBC Connection| MySQL
```
