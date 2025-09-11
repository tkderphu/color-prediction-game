package site.viosmash.common.instruction;

import site.viosmash.common.model.Round;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 10/09/2025
 */
public class SubmitRequest implements Serializable {
    private Round round;
    private List<String> guessedColors;

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }


    public List<String> getGuessedColors() {
        return guessedColors;
    }

    public void setGuessedColors(List<String> guessedColors) {
        this.guessedColors = guessedColors;
    }
}
