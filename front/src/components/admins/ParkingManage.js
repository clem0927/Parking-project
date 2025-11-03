import React, { useEffect, useState } from "react";
import axios from "axios";
import "../../css/ParkingManage.css";

const ParkingManage = () => {
    const [admins, setAdmins] = useState([]);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);

    // 데이터 불러오기
    const fetchAdmins = async () => {
        setLoading(true);
        try {
            const res = search
                ? await axios.get(`/api/admin/parks/search?pkltCd=${search}`)
                : await axios.get("/api/admin/parks");
            setAdmins(res.data);
        } catch (err) {
            console.error(err);
            setAdmins([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAdmins();
    }, [search]);

    // 삭제
    const handleDelete = async (pkltCd) => {
        if (!window.confirm(`주차장 ${pkltCd}를 삭제하시겠습니까?`)) return;
        try {
            await axios.delete(`/api/admin/parks/${pkltCd}`);
            alert("삭제 완료!");
            fetchAdmins();
        } catch (err) {
            console.error(err);
            alert("삭제 실패");
        }
    };

    return (
        <div className="parking-manage-container">
            <h2>🅿️ 주차장 관리</h2>
            <p>주차장 등록, 수정, 삭제 기능을 제공합니다.</p>

            {/* 검색 */}
            <div className="search-box">
                <input
                    type="text"
                    placeholder="주차장 번호로 검색..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
                <button onClick={fetchAdmins}>검색</button>
            </div>

            {loading ? (
                <p>로딩 중...</p>
            ) : (
                <div className="table-wrapper">
                    <table className="parking-table">
                        <thead>
                        <tr>
                            <th>주차장 번호 (PKLT_CD)</th>
                            <th>관리자 ID</th>
                            <th>삭제</th>
                        </tr>
                        </thead>
                        <tbody>
                        {admins.length === 0 ? (
                            <tr>
                                <td colSpan="3" style={{ textAlign: "center", color: "#888" }}>
                                    검색 결과가 없습니다.
                                </td>
                            </tr>
                        ) : (
                            admins.map((admin) => (
                                <tr key={admin.pkltCd}>
                                    <td>{admin.pkltCd}</td>
                                    <td>{admin.id}</td>
                                    <td>
                                        <button
                                            onClick={() => handleDelete(admin.pkltCd)}
                                            style={{
                                                backgroundColor: "#f44336",
                                                color: "#fff",
                                                border: "none",
                                                padding: "5px 10px",
                                                borderRadius: "5px",
                                                cursor: "pointer",
                                            }}
                                        >
                                            삭제
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default ParkingManage;
