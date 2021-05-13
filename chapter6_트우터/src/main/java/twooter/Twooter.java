package twooter;

import java.util.Optional;

public class Twooter {

    /**
     * 로그인 실패시 응답을 어떻게 할 것인가 ?
     *
     * 1. 예외발생 -> 문제해결은 가능하지만 적절한 예외를 사용했는가 ?
     * -> 로그온 실패는 언제든 일어날 수 있다. (아이디 / 패스워드 실패 등..)
     *
     * 2. null 을 반환 -> NPE 발생 가능성
     * -> 컴파일 타임에 체크가 불가능
     * -> 메소드 시그니쳐로 확인이 불가능
     *
     * * Optional 타입을 반환해 이를 해결한다.
     */
    public Optional<ServerEndPoint> onLogon(String userId, String password, ReceiverEndPoint receiver) {
        return Optional.ofNullable(null);
    }
}
