package ru.abstractmenus.data;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.hocon.api.serialize.NodeSerializeException;
import ru.abstractmenus.hocon.api.serialize.NodeSerializer;
import ru.abstractmenus.api.text.Colors;
import ru.abstractmenus.util.MiniMessageUtil;

import java.util.ArrayList;
import java.util.List;

public class BookData implements Cloneable {

    private String author;
    private String title;
    private List<String> pages = new ArrayList<>();

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getPages() {
        return MiniMessageUtil.parseToLegacy(pages);
    }

    public void setPages(List<String> pages) {
        this.pages = pages;
    }

    @Override
    public BookData clone(){
        try{
            BookData newData = (BookData) super.clone();
            newData.setPages(new ArrayList<>(this.pages));
            return (BookData) super.clone();
        } catch (CloneNotSupportedException e){
            return null;
        }
    }

    public static class Serializer implements NodeSerializer<BookData> {

        @Override
        public BookData deserialize(Class type, ConfigNode node) throws NodeSerializeException {
            BookData data = new BookData();

            if(node.node("author").rawValue() != null) {
                data.setAuthor(Colors.of(node.node("author").getString()));
            }

            if(node.node("title").rawValue() != null) {
                data.setTitle(Colors.of(node.node("title").getString()));
            }

            if(node.node("pages").rawValue() != null) {
                data.setPages(Colors.ofList(node.node("pages").getList(String.class)));
            }

            return data;
        }
    }
}
