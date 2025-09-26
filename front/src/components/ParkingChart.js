import React, { useMemo } from "react";
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid,
    Tooltip, Legend, ResponsiveContainer, ReferenceDot,
} from "recharts";

export default function ParkingChart({ parkName, csvDataByName }) {
    // 해당 주차장 데이터만 가져오기
    const data = useMemo(() => csvDataByName[parkName] || [], [csvDataByName, parkName]);

    const maxLive = data.length ? Math.max(...data.map(d => d.liveCnt)) : 0;
    const minLive = data.length ? Math.min(...data.map(d => d.liveCnt)) : 0;
    const maxPoint = data.find(d => d.liveCnt === maxLive);
    const minPoint = data.find(d => d.liveCnt === minLive);

    return (
        <div style={{ width: "100%", height: 400 }}>
            <div>{parkName} (2025-09-22)</div>
            <ResponsiveContainer width="100%" height="100%">
                <LineChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="time" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="liveCnt" stroke="#1f77b4" name="주차된 차량" dot={{ r: 3 }} />
                    <Line type="monotone" dataKey="remainCnt" stroke="#ff7f0e" name="남은 자리" dot={{ r: 3 }} />

                    {maxPoint && (
                        <ReferenceDot
                            x={maxPoint.time}
                            y={maxPoint.liveCnt}
                            r={6}
                            fill="red"
                            label={{ value: `최대: ${maxPoint.liveCnt}`, position: "top" }}
                        />
                    )}
                    {minPoint && (
                        <ReferenceDot
                            x={minPoint.time}
                            y={minPoint.liveCnt}
                            r={6}
                            fill="green"
                            label={{ value: `최소: ${minPoint.liveCnt}`, position: "bottom" }}
                        />
                    )}
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}
