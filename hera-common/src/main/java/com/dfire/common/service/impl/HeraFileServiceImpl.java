package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.vo.HeraFileVo;
import com.dfire.common.mapper.HeraFileMapper;
import com.dfire.common.mapper.HeraUserMapper;
import com.dfire.common.service.HeraFileService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.dfire.common.service.HeraUserService;
import com.dfire.common.tree.HeraFileTreeNode;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:20 2018/1/12
 * @desc
 */
@Service("heraFileService")
public class HeraFileServiceImpl implements HeraFileService {

   static String PERSONAL = "个人文档";
   static String SHARE = "共享文档";

    static String FILE = "2";
    static String FOLDER = "1";

    @Autowired
    private HeraFileMapper heraFileMapper;

    @Autowired
    private HeraUserService heraUserService;


    @Override
    public List<HeraFileVo> getSubHeraFiles(String owner) {
        List<HeraFile> heraFileList = heraFileMapper.getSubHeraFiles(owner);
        List<HeraFileVo> heraFileVos = heraFileList.stream().map(heraFile -> convert(heraFile)).collect(Collectors.toList());
        return heraFileVos;
    }



    @Override
    public HeraFileVo addHeraFile(HeraFile heraFile) {
        heraFileMapper.addHerFile(heraFile);
        return convert(heraFile);
    }

    @Override
    public void deleteHeraFile(String fileId) {
        heraFileMapper.deleteHeraFile(fileId);

    }

    @Override
    public void updateHeraFileContent(String fileId, String content) {
        HeraFile heraFile = heraFileMapper.getHeraFile(fileId);
        heraFile.setContent(content);
        heraFileMapper.update(heraFile);

    }

    @Override
    public void updateHeraFileName(String fileId, String name) {
        HeraFile heraFile = heraFileMapper.getHeraFile(fileId);
        if(heraFile.getParent() == null) {
            throw new  RuntimeException("不容许修改文件夹名字");
        }
        heraFile.setName(name);
        heraFileMapper.update(heraFile);

    }

    @Override
    public HeraFile getHeraFile(String id) {
        HeraFile heraFile = heraFileMapper.getHeraFile(id);
        return heraFile;
    }

    @Override
    public HeraFileTreeNode getUserFiles() {
        List<HeraFileVo> fileVoList = getUserFiles("biadmin");
        List<HeraFile> list = transformTOList(fileVoList);
        HeraFileVo heraFile = HeraFileVo.builder().name("根节点，不展示").id(String.valueOf(Integer.MIN_VALUE)).build();
        HeraFileTreeNode parent = new HeraFileTreeNode(heraFile);
        list.forEach(file -> {
            HeraFileTreeNode subNode = new HeraFileTreeNode(convert(file));
            subNode.setParentNode(parent);
            subNode.setParentId(parent.getId());
            parent.addChildNode(subNode);
            recursion(subNode);
        });

        return parent;
    }

    private void recursion(HeraFileTreeNode parent) {
        HeraFileVo heraFileVo = parent.getHeraFileVo();
        if(heraFileVo.isFolder()) {
            List<HeraFile> list = heraFileMapper.getSubHeraFiles(parent.getId());
            List<HeraFileVo> heraFileVos = convertTOList(list);
            heraFileVos.forEach(file -> {
                HeraFileTreeNode treeNode = new HeraFileTreeNode(file);
                treeNode.setParentNode(parent);
                treeNode.setParentId(parent.getId());
                parent.addChildNode(treeNode);
                recursion(treeNode);
            });
        }
    }

    @Override
    public List<HeraFileVo> getUserFiles(String owner) {
        List<HeraFile> heraFileList = heraFileMapper.getUserHeraFiles(owner);
        if(heraFileList.isEmpty() || heraFileList == null) {
            HeraFile personFile = HeraFile.builder().name(PERSONAL).type(FOLDER).owner(owner).build();
            heraFileMapper.addHerFile(personFile);
            HeraFile commonFile = HeraFile.builder().name(SHARE).type(FOLDER).owner(owner).build();
            heraFileMapper.addHerFile(commonFile);
            heraFileList.add(personFile);
            heraFileList.add(commonFile);
        }

        return convertTOList(heraFileList);
    }

    @Override
    public void moveHeraFile(String sourceId, String targetId) {
        HeraFile source = heraFileMapper.getHeraFile(sourceId);
        HeraFile target = heraFileMapper.getHeraFile(targetId);
        String owner = "";
        if(target.getType().equals("2") && target.getOwner().equals(source.getOwner())) {
            //权限控制暂时不做
            source.setParent(target.getId());
            heraFileMapper.update(source);
        } else {
            throw new RuntimeException("权限不足");
        }
    }

    @Override
    public List<HeraFileVo> getCommonFiles(HeraFileVo heraFileVo) {
        List<HeraFileVo> list = new ArrayList<>();
        if(heraFileVo == null) {
            List<HeraUser> userList = heraUserService.getAllUsers();
            for(HeraUser heraUser : userList) {
                if(!hasCommonFiles(heraUser)) {
                    continue;
                }
                HeraFileVo fileVo = HeraFileVo.builder().build();
                fileVo.setAdmin(false);
                fileVo.setFolder(true);
                fileVo.setId(heraUser.getName());
                fileVo.setName(heraUser.getName() + "(" + heraUser.getUid() +")");
                fileVo.setOwner(heraUser.getName());
                list.add(heraFileVo);
            }
        } else {
            List<HeraFileVo> heraFileList = getSubHeraFiles(heraFileVo.getOwner());
            list.addAll(heraFileList);
        }
        return list;
    }

    private boolean hasCommonFiles(HeraUser heraUser) {
        List<HeraFileVo> files = getSubHeraFiles(heraUser.getName());
        boolean b = false;
        for (HeraFileVo file : files) {
            if (file.getName().equalsIgnoreCase(SHARE)) {
                List<HeraFile> list = heraFileMapper.getSubHeraFiles(file.getId());
                if (list == null || list.isEmpty()) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return b;
    }


    @Override
    public void updateHostGroupId(String fileId, String hostGroupId) {
        HeraFile heraFile = heraFileMapper.getHeraFile(fileId);
        heraFile.setHostGroupId(hostGroupId);
        heraFileMapper.update(heraFile);

    }

    private  static List<HeraFileVo> convertTOList(List<HeraFile> list) {
        List<HeraFileVo> heraFileVos = list.stream().map(file -> convert(file)).collect(Collectors.toList());
        return heraFileVos;
    }

    private static HeraFileVo convert(HeraFile heraFile) {
        HeraFileVo heraFileVo = HeraFileVo.builder().build();
        BeanUtils.copyProperties(heraFile, heraFileVo);
        if(heraFile.getType().equals("2")) {
            heraFileVo.setFolder(false);
        } else if(heraFile.getType().equals("1")) {
            heraFileVo.setFolder(true);
        }
        //当前登录用户与file 的owner对比,暂时默认true
        heraFileVo.setAdmin(true);
        return heraFileVo;
    }

    private static HeraFile transform(HeraFileVo heraFileVo) {
        HeraFile heraFile = HeraFile.builder().build();
        BeanUtils.copyProperties(heraFileVo, heraFile);
        if(heraFileVo.isFolder()) {
            heraFile.setType(FOLDER);
        } else  {
            heraFile.setType(FILE);
        }
        //当前登录用户与file 的owner对比,暂时默认true
        heraFileVo.setAdmin(true);
        return heraFile;
    }

    private  static List<HeraFile> transformTOList(List<HeraFileVo> list) {
        List<HeraFile> heraFiles = list.stream().map(file -> transform(file)).collect(Collectors.toList());
        return heraFiles;
    }


}
