import {Link} from "react-router-dom";

const main=()=>{
    return(
        <>
            <h1>Parking</h1>
            <Link to="/admin">관리자</Link>
            <br></br>
            <Link to="/login">로그인</Link>
        </>
    )
}

export default main;

