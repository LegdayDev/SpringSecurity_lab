## Spring Security 1일차

### 프로젝트 생성 / 의존성 추가
> 1. 자동 설정의 의한 기본 보안 작동
> 2. 기본 보안 설정 클래스들
> 3. `HttpBasic` 방식 vs `FormLogin` 방식
---
### 1. 자동 설정의 의한 기본 보안 작동
> 스프링 시큐리티는 `SpringBootWebSecurityConfiguration` 클래스에서 자동 설정에 의한 기본 보안 설정 클래스가 생성된다.

- **Spring 서버가 가동되면 스프링 시큐리티의 초기화 작업 및 보안 설정**이 이루어진다.
- SpringBoot 의 기능으로 별도의 설정이나 코드를 작성하지 않고 동작한다.
  - **모든 요청에 대하여** 인증여부를 검증 -> _**승인된 인증만 자원에 접근**_
  - 인증방식은 `Form로그인` 방식과 `HttpBasic 로그인` 방식을 제공
  - 인증을 시도할 수 있는 기본 페이지를 제공하여 랜더링해준다.
  - 인증 승인이 이루어질 수 있도록 한 개의 계정이 기본적으로 제공
- 아래 코드는 `SpringBootWebSecurityConfiguration` 의 예제코드이다.
    ```java
    @Bean
    @Order(SecurityProperties.BASIC_AUTO_ORDER)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http){
            http.authorizeRequests().anyRequest().authenticated(); // 모든요청(anyRequest)에 인증(authenticated)를 받아야 한다.
            http.formLogin(); // 폼 로그인 인증방식 제공
            http.httpBasic(); // httpBasic 인증방식 제공
            
            return http.build();
    }
    ```
- 하지만 위 코드로는 다른 계정을 추가할 수 없고, 계정들의 권한을 체크할 수 있는 기능이 없다 !
---
### 2. 기본 보안 설정 클래스들
- 우선 기본 계정을 생성하는 클래스는 SecurityProperties 이다.
- 클래스 코드 내부를 보면 User 라는 클래스가 있고, 이 클래스를 통해 기본 계정을 생성한다.
    ```java
    @ConfigurationProperties(prefix = "spring.security")
    public class SecurityProperties {
            // 생략 ...
            
            public static class User {
    
            /**
             * Default user name.
             */
            private String name = "user";
    
            /**
             * Password for the default user name.
             */
            private String password = UUID.randomUUID().toString();
    
            /**
             * Granted roles for the default user name.
             */
            private List<String> roles = new ArrayList<>();
    
            private boolean passwordGenerated = true;
    
            public String getName() {
                return this.name;
            }
    
            public void setName(String name) {
                this.name = name;
            }
    
            public String getPassword() {
                return this.password;
            }
    
            public void setPassword(String password) {
                if (!StringUtils.hasLength(password)) {
                    return;
                }
                this.passwordGenerated = false;
                this.password = password;
            }
        
            // 생략 ..
    }
    ```
- 그리고 `SpringBootWebSecurityConfiguration` 내부에 `SecurityFilterChainConfiguration` 코드는 `@ConditionalOnDefaultWebSecurity` 어노테이션에 의해 특정 조건을 만족해야 실행된다.
    ```java
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    class SpringBootWebSecurityConfiguration {
            
            
            @Configuration(proxyBeanMethods = false)
            @ConditionalOnDefaultWebSecurity
            static class SecurityFilterChainConfiguration {
    
                @Bean
                @Order(SecurityProperties.BASIC_AUTH_ORDER)
                SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
                    http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated());
                    http.formLogin(withDefaults());
                    http.httpBasic(withDefaults());
                    return http.build();
                }
    }
    ```
- 조건 내부를 들여다보면 아래와 같은 코드가 나온다.
  - `@ConditionalOnClass` 어노테이션에 의해 `SecurityFilterChain`  클래스와 `HttpSecurity` 클래스가 **클래스 패스에 존재하는지 체크**한다.
  - `@ConditionalOnMissingBean` 어노테이션에 의해 `SecurityFilterChain` 클래스의 **Bean 이 생성되어 있지 않아야 참**이다.
  - 위 두 조건이 참이여야만 기본 보안 기능이 설정된다.
    ```java
    class DefaultWebSecurityCondition extends AllNestedConditions {
    
        DefaultWebSecurityCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }
    
        @ConditionalOnClass({ SecurityFilterChain.class, HttpSecurity.class })
        static class Classes {
    
        }
    
        @ConditionalOnMissingBean({ SecurityFilterChain.class })
        static class Beans {
    
        }
    
    }
    ```
---
### 3. `HttpBasic` 방식 vs `FormLogin` 방식
#### 1. Form 로그인 방식
> Form 로그인은 사용자가 웹 페이지의 로그인 양식을 통해 자신의 아이디와 비밀번호를 입력하고, 이 정보를 서버에 전송하여 인증을 받는 방식이다.
- **사용자 인터페이스 제공** : *사용자는 HTML 양식을 통해 자신의 정보를 입력*합니다.
- **맞춤형 인증** : *서버는 사용자의 로그인 정보를 받아 복잡한 로직을 처리*할 수 있다. 예를 들어, 로그인 실패 시 재시도 횟수 제한, 캡차 도입, 이중 인증 등 추가 보안 조치를 적용할 수 있다.
- **세션 관리** : 일반적으로 *세션 쿠키를 사용하여 사용자의 로그인 상태를 유지*한다. 이는 사용자가 사이트 내에서 페이지를 이동할 때마다 로그인을 유지할 수 있게 한다.
- **예시:**
  - 웹사이트에 로그인 페이지가 있고, 사용자가 이메일과 비밀번호를 입력한 후 '로그인' 버튼을 클릭한다. 서버는 이 정보를 받아 데이터베이스에서 확인하고, 일치하면 사용자에게 세션 쿠키를 발급하여 로그인 상태를 유지한다

#### 2. HTTP Basic Authentication (httpBasic 로그인 방식)
> HTTP Basic Authentication은 HTTP 헤더를 통해 사용자 아이디와 비밀번호를 인코딩된 형태로 서버에 전송하여 인증을 받는 방식이다. 이 정보는 매 요청마다 전송되어야 합니다.

- **간단한 인증 메커니즘** : 사용자 인터페이스 없이 *HTTP 헤더를 통해 인증 정보를 전송*한다. 이는 **API나 서버 간 통신에 적합**하다.
- **상태 비저장** : **`HTTP Basic`은 상태를 유지하지 않는(stateless) 프로토콜**이다. 따라서 *매 요청마다 사용자 인증 정보를 전송*한다.
- **보안 취약점** : **인증 정보가 Base64로 인코딩**되어 전송되기 때문에, *`HTTPS`를 사용하지 않으면 네트워크 스니핑을 통해 정보가 노출될 위험*이 있다.

- **예시:**
  - 클라이언트가 서버에 요청을 보낼 때, HTTP 헤더에 `Authorization: Basic <인코딩된 사용자명과 비밀번호>`를 포함하여 보낸다. 서버는 이 정보를 디코딩하여 유효성을 검사하고, 유효하다면 요청에 응답한다.

#### 차이점
- **사용자 경험** : Form 로그인은 사용자 친화적인 웹 인터페이스를 제공하는 반면, `HTTP Basic`은 API 또는 서버 간 통신에 주로 사용되며 사용자 인터페이스를 제공하지 않는다.
- **보안** : Form 로그인은 `HTTPS` 사용 시 보안이 강화된다. `HTTP Basic`은 항상 `HTTPS`를 사용해야 하며, 그렇지 않으면 정보가 쉽게 노출될 수 있다.
- **상태 관리** : Form 로그인은 세션을 통해 사용자 상태를 관리하지만, HTTP Basic은 상태 비저장 방식으로 매 요청마다 인증 정보를 전송해야한다.

