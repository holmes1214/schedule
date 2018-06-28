package com.evtape.schedule.web.base;

import com.evtape.schedule.domain.Permission;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lianhai on 2018/6/28.
 */
public class RolePermissionController {

    protected Map<String, Map<String, List<RolePermissionController.P>>> group(List<Permission> permissions,
                                                                               List<Integer> selectedIds) {

        Map<String, Map<String, List<RolePermissionController.P>>> levelMap = new HashMap<>();

        permissions.forEach(permission -> {

            String levelKey = permission.getLevel();
            String categoryKey = permission.getCategory();

            if (levelMap.containsKey(levelKey)) {
                // category为key
                Map<String, List<RolePermissionController.P>> categoryMap = levelMap.get(levelKey);
                if (categoryMap.containsKey(categoryKey)) {
                    // 若categoryMap中存在category的key, 则对应的value一定存在, 直接put
                    RolePermissionController.P rp = new RolePermissionController.P();
                    rp.setId(permission.getId());
                    rp.setName(permission.getName());
                    if (selectedIds.contains(permission.getId())) {
                        rp.setSelected(true);
                    }
                    categoryMap.get(categoryKey).add(rp);
                } else {
                    List<RolePermissionController.P> nameList = new ArrayList<>();
                    RolePermissionController.P rp = new RolePermissionController.P();
                    rp.setId(permission.getId());
                    rp.setName(permission.getName());
                    if (selectedIds.contains(permission.getId())) {
                        rp.setSelected(true);
                    }
                    nameList.add(rp);
                    categoryMap.put(categoryKey, nameList);
                }
            } else {
                Map<String, List<RolePermissionController.P>> categoryMap = new HashMap<>();
                List<RolePermissionController.P> nameList = new ArrayList<>();
                RolePermissionController.P rp = new RolePermissionController.P();
                rp.setId(permission.getId());
                rp.setName(permission.getName());
                if (selectedIds.contains(permission.getId())) {
                    rp.setSelected(true);
                }
                nameList.add(rp);
                categoryMap.put(categoryKey, nameList);
                levelMap.put(levelKey, categoryMap);
            }
        });
        return levelMap;
    }

    @Getter
    @Setter
    protected class P {
        int id;
        boolean selected = false;
        String name;
    }
}
