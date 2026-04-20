package com.example.appdraw.utils;

public class HomeworkHelper {

    public static class HomeworkDetails {
        public String desc;
        public String criteria1;
        public String criteria2;

        public HomeworkDetails(String desc, String criteria1, String criteria2) {
            this.desc = desc;
            this.criteria1 = criteria1;
            this.criteria2 = criteria2;
        }
    }

    public static HomeworkDetails getHomeworkDetails(String lessonTitle) {
        if (lessonTitle == null) {
            return getDefault();
        }

        String lowerTitle = lessonTitle.toLowerCase();

        if (lowerTitle.contains("chân dung") || lowerTitle.contains("khuôn mặt") || lowerTitle.contains("mắt") || lowerTitle.contains("môi")) {
            return new HomeworkDetails(
                    "Thực hành vẽ chân dung / ngũ quan theo góc độ đã học. Chú ý tỷ lệ kích thước các bộ phận và sắc độ đậm nhạt để tạo khối.",
                    "Độ chính xác về tỷ lệ và hình dáng",
                    "Xử lý sắc độ sáng tối tạo chiều sâu"
            );
        } else if (lowerTitle.contains("phong cảnh") || lowerTitle.contains("cảnh đêm") || lowerTitle.contains("cây") || lowerTitle.contains("bầu trời") || lowerTitle.contains("mây")) {
            return new HomeworkDetails(
                    "Vẽ lại bức phong cảnh theo phong cách của bạn. Hãy chú trọng vào lớp nền (background) và quy luật xa gần (Perspective).",
                    "Xử lý không gian và quy luật xa gần",
                    "Hiệu ứng ánh sáng và phối màu mượt mà"
            );
        } else if (lowerTitle.contains("màu nước") || lowerTitle.contains("loang màu") || lowerTitle.contains("chuyển màu")) {
            return new HomeworkDetails(
                    "Thực hành kỹ thuật loang màu nước (wet-on-wet hoặc wet-on-dry) như hướng dẫn trong bài học.",
                    "Kiểm soát lượng nước",
                    "Độ mượt mà của vệt loang màu"
            );
        } else if (lowerTitle.contains("anime") || lowerTitle.contains("chibi") || lowerTitle.contains("manga")) {
            return new HomeworkDetails(
                    "Dựng hình nhân vật Anime/Manga yêu thích của bạn. Chú ý tỷ lệ mắt to đặc trưng và cấu trúc xương hàm.",
                    "Sự sinh động của biểu cảm nhân vật",
                    "Độ gọn và sắc nét của nét viền (Lineart)"
            );
        } else if (lowerTitle.contains("động vật") || lowerTitle.contains("chó") || lowerTitle.contains("mèo") || lowerTitle.contains("chim")) {
            return new HomeworkDetails(
                    "Vẽ lại con vật trong bài học. Hãy thể hiện rõ chất liệu lông hoặc vảy bằng các nét bút (texture).",
                    "Thể hiện đúng kết cấu chất liệu (texture)",
                    "Tỷ lệ cơ thể động vật tự nhiên"
            );
        } else if (lowerTitle.contains("cơ bản") || lowerTitle.contains("bắt đầu") || lowerTitle.contains("cầm bút") || lowerTitle.contains("đường nét")) {
            return new HomeworkDetails(
                    "Thực hành vẽ các nét cơ bản, kiểm soát lực nhấn bút và độ dứt khoát của từng đường nét.",
                    "Độ tự tin và dứt khoát của đường nét",
                    "Sự gọn gàng trong bài tập"
            );
        }

        // Default
        return getDefault();
    }

    private static HomeworkDetails getDefault() {
        return new HomeworkDetails(
                "Thực hành lại tác phẩm trong bài học theo góc nhìn và phong cách của bạn. Đừng ngại sáng tạo thêm các chi tiết mới nhé!",
                "Bố cục tổng thể cân đối",
                "Cách lựa chọn và phối màu sắc"
        );
    }
}
