package site.viosmash.server;
public class ServerMain {
    public static void main(String[] args) throws Exception {
        ServerCore core = new ServerCore();
        core.startTcp(6000);
    }
}
