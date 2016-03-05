package com.mta.wordpress;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mta on 3/4/16.
 */
public class Post {
    private String postTitle;
    private String postId;
    private List<String> categories = new ArrayList<>();
    private String contents;

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postTitle='" + postTitle + '\'' +
                ", postId='" + postId + '\'' +
                ", categories=" + categories +
                ", contents='" + contents + '\'' +
                '}';
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void merge(Post p) {
        if (p.getContents() != null) {
            this.setContents(p.getContents());
        }
        if (p.getPostTitle() != null) {
            this.setPostTitle(p.getPostTitle());
        }
        if (p.getCategories() != null && p.getCategories().size() > 0) {
            this.setCategories(p.getCategories());
        }
        if (p.getPostId() != null) {
            this.setPostId(p.getPostId());
        }
    }
}
