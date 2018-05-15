package com.evtape.schedule.domain.vo;

import com.evtape.schedule.consts.ResponseMeta;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by lianhai on 2018/3/31.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class ResponseBundle {
    private Meta meta;
    private Object data;

    public ResponseBundle success() {
        this.meta = new Meta(ResponseMeta.SUCCESS);
        return this;
    }

    public ResponseBundle success(Object data) {
        this.meta = new Meta(ResponseMeta.SUCCESS);
        this.data = data;
        return this;
    }

    public ResponseBundle failure(ResponseMeta meta) {
        this.meta = new Meta(meta);
        return this;
    }

    public ResponseBundle failure(ResponseMeta meta, Object data) {
        this.meta = new Meta(meta);
        this.data = data;
        return this;
    }

    @Getter
    @Setter
    public class Meta {
        private Integer code;
        private String message;

        Meta(ResponseMeta meta) {
            this.code = meta.code();
            this.message = meta.message();
        }
    }
}
