// server/src/main/java/com/cgo/server/Score.java
package site.viosmash.server;
import java.util.List;

public class Score {
    // Quy tắc chấm điểm:
    // - Đúng tất cả: +1
    // - Đúng một phần: +0.15/màu đúng
    public static float calcScore(List<String> answer, List<String> truth) {
        if (answer == null || answer.isEmpty()) return 0.0f;
        int correct = 0;
        for(int j = 0; j < answer.size(); j++) {
            for(int i = 0; i < truth.size(); i++) {
                if (truth.get(i).equalsIgnoreCase(answer.get(j))) {
                    correct++;
                    break;
                }
            }
        }

        if (correct == truth.size()) return 1.0f;
        return Math.min(1.0f, correct * 0.15f);
    }
}
