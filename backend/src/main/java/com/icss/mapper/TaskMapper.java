package com.icss.mapper;

import com.icss.model.ServiceTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    List<ServiceTask> selectTasks(@Param("level") Integer level,
                                  @Param("workflowCode") String workflowCode,
                                  @Param("stageCode") String stageCode,
                                  @Param("customerId") String customerId,
                                  @Param("status") String status);

    ServiceTask selectById(String id);

    int insert(ServiceTask task);

    int update(ServiceTask task);

    int updateStatus(@Param("id") String id,
                     @Param("status") String status,
                     @Param("updatedAt") Long updatedAt);

    int grab(@Param("id") String id,
             @Param("assignedTo") String assignedTo,
             @Param("updatedAt") Long updatedAt);
}
