package com.evtape.schedule.support.service;

import org.apache.commons.lang3.RandomUtils;

import java.util.Stack;

/**
 * Created by holmes1214 on 04/08/2017.
 * Dijkstra求单源最短路径 2010.8.26
 */
public class Dijkstra {

    public static void DijkstraPath(int[][] matrix, int[] dist, int[] path, int v0) {
        int i, j, k;
        int n=dist.length;
        boolean[] visited = new boolean[n];
        for (i = 0; i < n; i++) {

            if (matrix[v0][i] < 0 && i != v0) {
                dist[i] = matrix[v0][i];
                path[i] = v0;     //path记录最短路径上从v0到i的前一个顶点
            } else {
                dist[i] = 0;    // 若i不与v0直接相邻，则权值置为无穷大
                path[i] = -1;
            }
            visited[i] = false;
            path[v0] = v0;
            dist[v0] = 0;
        }
        visited[v0] = true;

        for (i = 1; i < n; i++) {
            //循环扩展n - 1 次

            int min = 0;

            int u = 0;

            // 寻找未被扩展的权值最小的顶点
            for (j = 0; j < n; j++)    {
                if (visited[j] == false && dist[j] < min) {
                    min = dist[j];
                    u = j;
                }
            }
            visited[u] = true;
            // 更新dist数组的值和路径的值
            for (k = 0; k < n; k++) {
                if (visited[k] == false
                        && matrix[u][k] < 0
                        && min + matrix[u][k] > dist[k]) {
                    dist[k] = min + matrix[u][k];
                    path[k] = u;
                }
            }
        }
    }


    //  打印最短路径上的各个顶点
    public static void showPath(int[] path, int v, int v0) {
        Stack<Integer> s = new Stack<>();

        while (v != v0) {
            s.push(v);
            v = path[v];
        }
        s.push(v);

        while (!s.isEmpty()) {
                System.out.print(s.peek());
                s.pop();
        }
    }


    public static void main(String[] argv) {

        int n = 59;     // 表示输入的顶点数和边数
        int i, j;

        int[][] matrix=new int[n][];
        int v0=0;//输入源顶点
        int[] dist = new int[n];
        int[] path = new int[n];

        for (i = 0; i < n; i++) {
            matrix[i]=new int[n];
            for (j = 0; j < n; j++) {
                int r = RandomUtils.nextInt(0, 3);
                if (r==2){
                    matrix[i][j] = -1;
                }else {
                    matrix[i][j]=0;
                }
            }
        }

        DijkstraPath(matrix, dist, path, v0);

        for (i = 0; i < n; i++) {

            if (i != v0) {
                try {
                    showPath(path, i, v0);
                    System.out.println(dist[i]);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
