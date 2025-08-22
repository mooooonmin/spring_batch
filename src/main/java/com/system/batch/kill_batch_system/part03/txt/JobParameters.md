# JobParameters
- 배치 작업에 전달되는 입력 값
- 배치가 어떤 조건에서, 어떤 데이터를 다룰지 결정하는 핵심 역할
- Spring Batch는 JobParameters의 모든 값을 메타데이터 저장소에 기록
  - Job 인스턴스 식별 및 재시작 처리
  - Job 실행 이력 추적
---
### JobParameters 전달하기
- 커맨드라인에서 JobParameters 전달하기
  - 가장 흔히 사용되는 시나리오 중 하나는 파일 경로를 JobParameters로 넘기는 경우
  
```
./gradlew bootRun --args='--spring.batch.job.name=dataProcessingJob inputFilePath=/data/input/users.csv,java.lang.String'
```
  - `--spring.batch.job.name`으로 전달한 Job의 이름 뒤에 따라오는 `inputFilePath=/data/input/users.csv,java.lang.String` 이 JobParameters
  - `--spring.batch.job.name`은 실행할 Job의 이름을 지정하는 Spring Boot의 애플리케이션 아규먼트이며, 그 뒤의 key=value,type들이 실제 Job에 주입되는 Spring Batch JobParameters이다.
---
### JobParameters 기본 표기법
```
parameterName=parameterValue,parameterType,identificationFlag
```
- parameterName: 
  - 배치 Job에서 파라미터를 찾을 때 사용할 key 값이다. 
  - 이 이름으로 Job 내에서 파라미터에 접근할 수 있다.
- parameterValue: 파라미터의 실제 값
- parameterType: 
  - 파라미터의 타입(`java.lang.String`, `java.lang.Integer`와 같은 fully qualified name 사용). 
  - 이 파라미터 타입을 명시하지 않을 경우 Spring Batch는 해당 파라미터를 String 타입으로 가정한다.
- identificationFlag: 
  - Spring Batch에게 해당 파라미터가 JobInstance 식별(identification)에 사용될 파라미터인지 여부를 전달하는 값으로 true이면 식별에 사용된다는 의미이다. 
  - 이 플래그는 생략 가능하며 생략할 경우 true로 설정된다.
---

### 날짜와 시간 파라미터 전달 주의사항
```
./gradlew bootRun --args='--spring.batch.job.name=terminatorJob executionDate=2024-01-01,java.time.LocalDate startTime=2024-01-01T14:30:00,java.time.LocalDateTime'
```
- executionDate는 `yyyy-MM-dd` 형식을, startTime은 `yyyy-MM-ddThh:mm:ss` 형식을 사용
- `ISO_LOCAL_DATE`와 `ISO_LOCAL_DATE_TIME` 형식과 일치 
-  날짜/시간 타입의 잡 파라미터는 이러한 ISO 표준 형식으로 전달해야 한다.
---
