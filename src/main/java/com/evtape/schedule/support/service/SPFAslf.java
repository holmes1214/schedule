package com.evtape.schedule.support.service;

/**
 * Created by holmes1214 on 06/08/2017.
 */
public class SPFAslf {
    class Data {

        int from, to, next, v;
    }

    int n, m, ans;
    int ne;
    int head[];
    boolean inq[], del[];
    int q[], dis[], father[];
    Data[] e;

    void insert(int u, int v, int w) {
        ne++;
        e[ne].from = u;
        e[ne].to = v;
        e[ne].v = w;
        e[ne].next = head[u];
        head[u] = ne;
    }

    void spfa(int k) {
//        memset(dis, 127, sizeof(dis));
//        memset(inq, 0, sizeof(inq));
        int t = 0, w = 1, now;
        q[t] = 1;
        inq[1] = true;
        dis[1] = 0;
        while (t < w) {
            int p = head[q[t]];
            now = q[t];
            t++;
            while (p > 0) {
                if (!del[p] && dis[now] + e[p].v < dis[e[p].to]) {
                    dis[e[p].to] = dis[now] + e[p].v;
                    if (k == 1) father[e[p].to] = p;
                    if (!inq[e[p].to]) {
                        inq[e[p].to] = true;
                        q[w++] = e[p].to;
                    }
                }
                p = e[p].next;
            }
            inq[now] = false;
        }
    }

    int main() {
//        scanf("%d%d", & n,&m);
        for (int i = 0; i < m; i++) {
            int x, y, z;
//            scanf("%d%d%d", & x,&y,&z);
//            insert(x, y, z);
//            insert(y, x, z);
        }
        spfa(1);
        ans = dis[n];
        for (int i = n; i != 1; i = e[father[i]].from) {
            del[father[i]] = true;
            spfa(0);
            del[father[i]] = false;
            ans = Math.max(dis[n], ans);
        }
//        printf("%d", ans);
        return 0;
    }
}
