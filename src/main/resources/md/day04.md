## Spring Security 4일차

### DelegatingFilterProxy / FilterChainProxy
> 1. Filter
> 2. DelegatingFilterProxy

---
### 1. Filter
- `서블릿 필터(Servlet Filter)`는 웹 어플리케이션에서 **클라이언트의 요청과 서버의 응답을 가공하거나 검사**하는데 사용한다.
- 서블릿 필터는 **클라이언트의 요청이 서블릿에 도달하기 전이나 서블릿의 응답을 클라이언트에게 보내기 전에 특정 작업을 수행**할 수 있다.
- 서블릿 필터는 WAS(서블릿 컨테이너 또는 Tomcat)에서 `생성(init)`되고 `실행(doFilter)`되고 `종료(destroy)`된다.

    ![img.png](../static/images/day04/img01.png)
- 예시코드는 아래와 같다.
    ```java
    public class ExampleFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // 필터 초기화 시 필요작업 수행
        }
    
        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
            // 요청 처리 전에 수행할 작업(pre-processing)
            chain.doFilter(req, resp); // 다음 필터로 요청/응답 객체 전달
            // 응답 처리 후에 수행할 작업(post-processing)
        }
        
        @Override
        public void destroy(){
            // 필터가 제거될 때 필요한 정리 작업을 수행
        }
    }
    ```
---
### 2. DelegatingFilterProxy
> `DelegatingFilterProxy` 는 실제 보안처리를 하는 필터가 아닌 스프링 컨테이너(IoC)와 연결고리를 하는 필터이다.

- `DelegatingFilterProxy` 는 스프링에서 사용되는 특별한 서블릿 필터이다.
- `DelegatingFilterProxy` 는 서블릿 필터의 기능을 수행하는 동시에 ***스프링 의존성 주입(DI) 및 Bean 관리 기능과 연동되도록 설계된 필터***이다.
- `DelegatingFilterProxy` 는 `springSecurityFilterChain` 이름으로 생성된 `Bean` 을 `ApplicationContext` 에서 찾아 ***요청을 위임***한다.
- _**실제 보안 처리를 수행하지 않는다.**_

    ![img_1.png](../static/images/day04/img02.png)