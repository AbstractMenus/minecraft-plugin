package ru.abstractmenus.menu.generated;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.abstractmenus.api.Catalog;
import ru.abstractmenus.api.inventory.Item;
import ru.abstractmenus.menu.SimpleMenu;
import ru.abstractmenus.menu.item.MenuItem;
import ru.abstractmenus.util.ArrayListIterator;

import java.util.*;

public class GeneratedMenu extends SimpleMenu {

    private Catalog<?> catalog;
    private Matrix matrix;

    private final Map<Integer, Object> slotContexts = new HashMap<>();

    private ArrayList<?> snapshot;
    private Object currentObject;
    private int perPage;
    private int page;
    private int pages;

    public GeneratedMenu(String title, int size) {
        super(title, size);
    }

    public Catalog<?> getCatalog() {
        return catalog;
    }

    public List<?> getSnapshot() {
        return snapshot;
    }

    public Object getCurrentObject() {
        return currentObject;
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public void nextPage(Player player, int skip) {
        if (changePage(skip))
            refresh(player);
    }

    public void prevPage(Player player, int skip) {
        if (changePage(-skip))
            refresh(player);
    }

    private boolean changePage(int value) {
        int current = page;
        page = page + value;
        page = Math.max(0, page);
        page = Math.min(page, pages - 1);
        return current != page;
    }

    @Override
    public boolean open(Player player) {
        if (checkOpenRules(player)) {
            openListener.onOpen(player, this);

            if (preOpenActions != null)
                preOpenActions.activate(player, this, null);

            createInventory(player, this);

            if(openActions != null)
                openActions.activate(player, this, null);

            snapshot = new ArrayList<>(catalog.snapshot(player, this));
            perPage = matrix.getSlots().size();
            pages = snapshot.size() / perPage;

            if (snapshot.size() % perPage > 0) pages++;

            refresh(player);

            player.openInventory(inventory);

            if (postOpenActions != null)
                postOpenActions.activate(player, this, null);
            return true;
        }

        if(denyActions != null)
            denyActions.activate(player, this, null);

        return false;
    }

    @Override
    public void refresh(Player player) {
        super.refresh(player);

        slotContexts.clear();

        snapshot = new ArrayList<>(catalog.snapshot(player, this));

        perPage = matrix.getSlots().size();
        pages = snapshot.size() / perPage;

        if (snapshot.size() % perPage > 0) pages++;



        int from = page * perPage;
        ArrayListIterator<?> iterator = new ArrayListIterator<>(snapshot);

        iterator.skip(from);

        for (int slot : matrix.getSlots()) {
            if (!iterator.hasNext()) break;

            currentObject = iterator.next();
            Item item;
            if (currentObject instanceof Item currentItem) {
                Item matrixItem = matrix.getItem(slot);
                currentItem.setProperties(matrixItem.getProperties());
                item = currentItem;
            } else {
                item = matrix.getItem(slot).clone();
            }

            if (checkItemRules(item, player))
                setItem(slot, player, item, currentObject);
        }
    }

    @Override
    public void click(int slot, Player player, ClickType type) {
        Object ctx = slotContexts.get(slot);
        if (ctx != null) currentObject = ctx;
        super.click(slot, player, type);
    }

    public void setCatalog(Catalog<?> catalog){
        this.catalog = catalog;
    }

    public void setMatrix(Matrix matrix){
        this.matrix = matrix;
    }

    @Override
    public GeneratedMenu clone() {
        return (GeneratedMenu) super.clone();
    }

    private void setItem(int slot, Player player, Item item, Object ctx) {
        ItemStack stack = item.build(player, this);

        slotContexts.put(slot, ctx);
        showedItems.put(slot, item);
        inventory.setItem(slot, stack);
    }

    private boolean checkItemRules(Item item, Player player) {
        if (item instanceof MenuItem) {
            return ((MenuItem) item).checkShowRules(player, this);
        }
        return true;
    }
}
