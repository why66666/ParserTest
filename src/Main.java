public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String filePath = "D:\\test\\javaFile\\BlogBaseService.java";

        try {
            ConnCloseCheck.getInstance().doCheck(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("time:"+ (endTime - startTime) + "ms");
    }
}
