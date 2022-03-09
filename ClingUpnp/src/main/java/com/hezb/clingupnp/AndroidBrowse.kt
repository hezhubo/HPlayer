package com.hezb.clingupnp

import org.fourthline.cling.model.action.ActionException
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.ErrorCode
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.SortCriterion

/**
 * Project Name: HPlayer
 * File Name:    AndroidBrowse
 *
 * Description: Browse回调  解决XML解析错误问题.
 *
 * @author  hezhubo
 * @date    2022年03月06日 17:38
 */
abstract class AndroidBrowse(
    service: Service<*, *>,
    containerId: String?,
    flag: BrowseFlag,
    filter: String = "*",
    firstResult: Long = 0,
    maxResults: Long? = null,
    vararg orderBy: SortCriterion?
) : Browse(service, containerId, flag, filter, firstResult, maxResults, *orderBy) {

    override fun success(invocation: ActionInvocation<out Service<*, *>>) {
        val result = BrowseResult(
            invocation.getOutput("Result").value.toString(),
            invocation.getOutput("NumberReturned").value as UnsignedIntegerFourBytes,
            invocation.getOutput("TotalMatches").value as UnsignedIntegerFourBytes,
            invocation.getOutput("UpdateID").value as UnsignedIntegerFourBytes
        )
        val proceed = receivedRaw(invocation, result)
        if (proceed && result.countLong > 0L && result.result.isNotEmpty()) {
            try {
                val didlParser = AndroidDIDLParser()
                val didl = didlParser.parse(result.result)
                received(invocation, didl)
                updateStatus(Status.OK)
            } catch (var6: Exception) {
                invocation.failure = ActionException(
                    ErrorCode.ACTION_FAILED,
                    "Can't parse DIDL XML response: $var6", var6
                )
                this.failure(invocation, null as UpnpResponse?)
            }
        } else {
            received(invocation, DIDLContent())
            updateStatus(Status.NO_CONTENT)
        }
    }

}