// server/src/main/java/com/cgo/server/Score.java
package site.viosmash.server;
import java.util.List;

public class Score {
    // Quy tắc chấm điểm:
    // - Đúng tất cả: +1
    // - Đúng một phần: +0.2/màu đúng (không vượt 1.0)
    public static double calcScore(List<String> answer, List<String> truth) {
        if (answer == null || answer.isEmpty()) return 0.0;
        int correct = 0;
        int n = Math.min(answer.size(), truth.size());
        for (int i=0;i<n;i++) {
            if (truth.get(i).equalsIgnoreCase(answer.get(i))) correct++;
        }
        if (correct == truth.size()) return 1.0;
        return Math.min(1.0, correct * 0.2);
    }
}
