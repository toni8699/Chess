package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Stockfish {
    private Process process;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    private final String STOCKFISH_PATH = "/path/to/stockfish"; // Placeholder path

    public boolean startEngine() {
        try {
            process = new ProcessBuilder(STOCKFISH_PATH).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new OutputStreamWriter(process.getOutputStream());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendCommand(String command) {
        try {
            writer.write(command + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOutput(int waitTime) {
        StringBuilder output = new StringBuilder();
        try {
            Thread.sleep(waitTime);
            sendCommand("isready");
            while (true) {
                String line = reader.readLine();
                if (line.contains("readyok")) {
                    break;
                }
                output.append(line).append("\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public String getBestMove(String fen, int waitTime) {
        sendCommand("position fen " + fen);
        sendCommand("go movetime " + waitTime);
        String output = getOutput(waitTime + 20);
        return output.split("bestmove ")[1].split(" ")[0];
    }

    public void stopEngine() {
        try {
            sendCommand("quit");
            process.destroy();
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
