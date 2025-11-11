package org.xioamila.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xioamila.entity.MusicMenuRelation;
import org.xioamila.mapper.MusicMenuRelationMapper;
import org.xioamila.service.MusicMenuRelationService;

@Slf4j
@Service
@AllArgsConstructor
public class MusicMenuRelationServiceImpl extends ServiceImpl<MusicMenuRelationMapper, MusicMenuRelation> implements MusicMenuRelationService {

}
