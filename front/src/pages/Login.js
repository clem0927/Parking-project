import { useState } from "react";
import { Link } from "react-router-dom";
import { Modal, Button, Form } from "react-bootstrap";

const Login = () => {
    const [show, setShow] = useState(false);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    return (
        <>
            <Link to="/">메인 페이지로</Link>
            <h1>로그인</h1>
            <form>
                <fieldset>
                    아이디 <input type="text" />
                </fieldset>
                <fieldset>
                    비밀번호 <input type="password" />
                </fieldset>
                <button type="submit">로그인</button>
                <hr />
                <button type="button" onClick={handleShow}>회원가입</button>
            </form>

            {/* 회원가입 모달 */}
            <Modal show={show} onHide={handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>회원가입</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>아이디</Form.Label>
                            <Form.Control type="text" placeholder="아이디 입력" />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>비밀번호</Form.Label>
                            <Form.Control type="password" placeholder="비밀번호 입력" />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose}>
                        닫기
                    </Button>
                    <Button variant="primary" onClick={handleClose}>
                        가입하기
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};

export default Login;
