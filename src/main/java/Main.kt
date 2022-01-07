
const val WORD_SIZE = 5
const val MAX_GUESSES = 6

enum class GuessResult(val character: Char) {
    DOES_NOT_EXIST('x'),
    EXISTS_IN_DIFFERENT_SPOT('y'),
    CORRECT('g'),
}

class FiveLetterWord(
    val asWord: String,
) {
    var asHint = "x".repeat(WORD_SIZE)
    override fun toString(): String = asWord
    operator fun get(n: Int): Char = asWord[n]
    operator fun set(n: Int, value: GuessResult) {
        val temp = asHint.toCharArray()
        temp[n] = value.character
        asHint = temp.toString()
    }
    fun isCorrect(): Boolean = asHint.all { it == GuessResult.CORRECT.character }
}

class WordleBot(
    word: String = "roate",
) {
    private var firstGuess = FiveLetterWord(word)

    private var allWords =
        WordleBot::class.java.getResource("guesses")
            ?.let { text -> text.readText().split("\r\n").map { FiveLetterWord(it) } }
            ?: error("No such resource")
    private var possibleWords =
        WordleBot::class.java.getResource("solutions")
            ?.let { text -> text.readText().split("\r\n").map { FiveLetterWord(it) } }
            ?: error("No such resource")

    private fun isWordPossible(
        guess: FiveLetterWord,
        word: FiveLetterWord,
    ): Boolean {
        val guessAsHint = guess.asHint
//        check greens
        for (n in 0 until WORD_SIZE) {
            if (guessAsHint[n] == GuessResult.CORRECT.character) {
                if (guess[n] != word[n]) return false
                word[n] = GuessResult.DOES_NOT_EXIST
            }
        }
//        check yellows
        for (n in 0 until WORD_SIZE) {
            if (guessAsHint[n] == GuessResult.EXISTS_IN_DIFFERENT_SPOT.character) {
                if (guess[n] == word[n]) return false // this would have been green, not yellow, so it fails
                var found = false
                for (m in 0 until WORD_SIZE) {
                    if (guess[n] == word[m]) {
                        found = true
                        word[m] = GuessResult.DOES_NOT_EXIST
                        break
                    }
                }
                if (!found) return false
            }
        }
//        check greys
        for (n in 0 until WORD_SIZE)
            if (guessAsHint[n] == GuessResult.DOES_NOT_EXIST.character)
                if (guess[n] == word[n]) return false

        return true
    }

    private fun evaluateGuess(guess: FiveLetterWord, actual: FiveLetterWord): Int {
        val result = Array(5) { 0 }
//        green
        for (n in 0 until WORD_SIZE) {
            if (guess[n] == actual[n]) {
                result[n] = GuessResult.CORRECT.ordinal
                actual[n] = GuessResult.DOES_NOT_EXIST
            }
        }
//        yellow
        for (n in 0 until WORD_SIZE) {
            if (result[n] != GuessResult.CORRECT.ordinal) {
                for (m in 0 until WORD_SIZE) {
                    if (guess[n] == actual[m]) {
                        result[n] = GuessResult.EXISTS_IN_DIFFERENT_SPOT.ordinal
                        actual[m] = GuessResult.DOES_NOT_EXIST
                        break
                    }
                }
            }
        }
        return result.sum()
    }

    private fun bestGuess(guesses: List<FiveLetterWord>, solutions: List<FiveLetterWord>): FiveLetterWord {
        var bestAvgScore = 0
        var bestAvgWord = FiveLetterWord(firstGuess.asWord)

        guesses.map { guess ->
            var avgScore = 0
            solutions.map { solution ->
                avgScore += evaluateGuess(guess, solution)
            }

            if (avgScore > bestAvgScore) {
                bestAvgScore = avgScore
                bestAvgWord = guess
            }
        }

        return bestAvgWord
    }

    fun solve() {
        for (n in 0 until MAX_GUESSES) {
            println("My guess is: $firstGuess")
            println("Enter the result ('x' - unknown, 'g' - correct, 'y' - wrong place")
            firstGuess.asHint = readln().take(WORD_SIZE)
            if (firstGuess.isCorrect()) {
                println("Nice! The word is: ${firstGuess.asWord}")
                break
            }
            possibleWords = possibleWords.filter { isWordPossible(firstGuess, it) }
            if (possibleWords.isEmpty()) {
                println("Impossible to guess")
                break
            }
            println("Possible words are:")
            possibleWords.map { println(it.asWord) }
            firstGuess = bestGuess(allWords, possibleWords)
        }
    }
}

fun main() {
    WordleBot("raise").solve()
}
