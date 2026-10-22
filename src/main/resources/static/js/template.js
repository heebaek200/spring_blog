/**
 * 게시글의 제목과 본문을 AI 교정 API에 전달합니다.
 * Fetch API를 이용해 비동기 POST 요청을 전송합니다.
 * 서버에서 반환한 교정 결과를 화면에 출력합니다.
 */
async function proofread() {
    const content = document.querySelector("[name=\"content\"]").value;
    const result = document.querySelector("#proofreadResult");

    // 교정 요청 시작
    result.textContent = "AI 교정 중...";

    try {
        const response = await fetch("/api/agent/proofread", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({ content })
        });

        // HTTP 응답 상태 확인
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        // 교정 결과 출력
        result.textContent = await response.text();

    } catch (error) {
        console.error("AI 교정 실패:", error);
        result.textContent = "AI 교정 중 오류가 발생했습니다.";
    }
}