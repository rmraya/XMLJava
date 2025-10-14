/*******************************************************************************
 * Copyright (c) 2022 - 2025 Maxprograms.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Commercial licenses are available at https://maxprograms.com/
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/
package com.maxprograms.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class PI implements XMLNode {

	private static final long serialVersionUID = -689697302415200547L;
	private String target;
	private String data;

	public PI(String target, String data) {
		this.target = target;
		this.data = data;
	}

	public String getTarget() {
		return target;
	}

	public String getData() {
		return data;
	}

	public void setData(String value) {
		data = value;
	}

	@Override
	public String toString() {
		return "<?" + target + " " + data + "?>";
	}

	@Override
	public void writeBytes(OutputStream output, Charset charset) throws IOException {
		output.write(toString().getBytes(charset));
	}

	@Override
	public short getNodeType() {
		return XMLNode.PROCESSING_INSTRUCTION_NODE;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PI)) {
			return false;
		}
		PI pi = (PI) obj;
		return target.equals(pi.getTarget()) && data.equals(pi.getData());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
