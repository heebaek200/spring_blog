package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class ReplyResponse {

    @Data
    public static class ListDto {
        private Long id;
        private String comment;
        private Long userId;
        private boolean isOwner;
        private String username;
        private String createdAt;

        public ListDto(Reply reply, Long sessionuserId) {
            this.id = reply.getId();
            this.comment = reply.getComment();

            if (reply.getUser() != null) {
                this.userId = reply.getUser().getId();
                this.isOwner = reply.isOwner(sessionuserId);
                this.username = reply.getUser().getUsername();
            }

            if (reply.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(reply.getCreatedAt());
            }
        }
    }

}
