public class Main {
    public static void main(String[] args) {
        String filePath = "D:\\Work\\WorkFile\\SysUpdateWF.java";
        try {
            ConnCloseCheck.getInstance().doCheck(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
