package lab.dynafig.zookeeper;

import lab.dynafig.Updating;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Closeable;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode.POST_INITIALIZED_EVENT;

/**
 * {@code ZooKeeperListener} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">B. K. Oxley</a>
 * @todo Needs documentation
 * @todo PathChildrenCache redundant to DefaultDynafig's cache
 */
public class ZooKeeperListener
        implements Closeable, PathChildrenCacheListener {
    private final Updating updating;
    private final PathChildrenCache cache;

    @Inject
    public ZooKeeperListener(@Nonnull final Updating updating,
            @Nonnull final CuratorFramework client,
            @Nonnull final String zpath) {
        this.updating = updating;
        cache = new PathChildrenCache(client, zpath, true);
    }

    @PostConstruct
    public void init()
            throws Exception {
        cache.start(POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(this);
    }

    @Override
    public void close()
            throws IOException {
        cache.close();
    }

    @Override
    public void childEvent(final CuratorFramework client,
            final PathChildrenCacheEvent event)
            throws Exception {
        switch (event.getType()) {
        case INITIALIZED:
        case CHILD_ADDED:
        case CHILD_UPDATED:
            final ChildData childData = event.getData();
            updating.update(childData.getPath(),
                    new String(childData.getData(), UTF_8));
            break;
        case CHILD_REMOVED:
            break;
        case CONNECTION_SUSPENDED:
            break;
        case CONNECTION_RECONNECTED:
            break;
        case CONNECTION_LOST:
            break;
        }
    }
}
